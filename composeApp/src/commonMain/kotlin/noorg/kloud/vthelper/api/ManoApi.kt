package noorg.kloud.vthelper.api

import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastZip
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.Url
import io.ktor.http.parameters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.serialization.json.Json
import noorg.kloud.vthelper.api.ManoApi.getCompletedSemesterResultsUnsafe
import noorg.kloud.vthelper.api.ManoApi.getEmployeeDetailsUnsafe
import noorg.kloud.vthelper.api.ManoApi.getEmployeesUnsafe
import noorg.kloud.vthelper.api.ManoApi.getSettlementGradesUnsafe
import noorg.kloud.vthelper.api.ManoApi.getStudentInfoUnsafe
import noorg.kloud.vthelper.api.ManoApi.getSubjectTimetableUnsafe
import noorg.kloud.vthelper.api.ManoApi.getThisSemesterInfoUnsafe
import noorg.kloud.vthelper.api.VTBaseApi.getPageWithSamlRefresh
import noorg.kloud.vthelper.api.models.NetResult
import noorg.kloud.vthelper.api.models.expect200
import noorg.kloud.vthelper.api.models.mano.ApiManoBasicDepartmentData
import noorg.kloud.vthelper.api.models.mano.ApiManoBasicOfficeData
import noorg.kloud.vthelper.api.models.mano.ApiManoCalloutData
import noorg.kloud.vthelper.api.models.mano.ApiManoCourseTimetableEntity
import noorg.kloud.vthelper.api.models.mano.ApiManoEmployeeBasicEntity
import noorg.kloud.vthelper.api.models.mano.ApiManoEmployeeDetails
import noorg.kloud.vthelper.api.models.mano.ApiManoStudentInfo
import noorg.kloud.vthelper.api.models.mano.ApiManoSubjectExamInfo
import noorg.kloud.vthelper.api.models.mano.ApiManoThisSemesterInfo
import noorg.kloud.vthelper.api.models.mano.ApiManoThisSemesterSubjectEntity
import noorg.kloud.vthelper.api.models.mano.ApiManoTimetableEntityType
import noorg.kloud.vthelper.api.models.mano.ApiManoTimetableEntityWeek
import noorg.kloud.vthelper.api.models.mano.ManoTimetableWeekday
import noorg.kloud.vthelper.api.models.mano.grades.ApiManoCompletedSemesterResult
import noorg.kloud.vthelper.api.models.mano.grades.ApiManoSemesterMediateResults
import noorg.kloud.vthelper.api.models.mano.grades.ApiManoSubjectEvaluationVerdict
import noorg.kloud.vthelper.api.models.mano.grades.ApiManoSubjectFinalResult
import noorg.kloud.vthelper.api.models.mano.grades.ApiManoSubjectSettlementGrade
import noorg.kloud.vthelper.api.models.mano.grades.ApiManoSubjectSettlementOverview
import noorg.kloud.vthelper.api.models.mano.grades.EvaluationVerdictLookup
import noorg.kloud.vthelper.api.models.toNetResultFail
import noorg.kloud.vthelper.api.models.toNetResultOk
import noorg.kloud.vthelper.nullIfDash
import noorg.kloud.vthelper.findFirstGroup
import noorg.kloud.vthelper.nullIfBlank
import noorg.kloud.vthelper.platform_specific.getHttpClientBase
import noorg.kloud.vthelper.toFloatDashAsNull
import noorg.kloud.vthelper.toIntDashAsNull
import noorg.kloud.vthelper.toIntNotNull
import noorg.kloud.vthelper.toRelativeSemester
import kotlin.concurrent.Volatile
import kotlin.time.Duration
import kotlin.time.Instant


object ManoApi {

    private val baseUrl = Url("https://mano.vilniustech.lt")
    private val jsonSerializer = Json { ignoreUnknownKeys = true }

    private const val UPDATE_SESSION_OP = "update session"

    private val unicodeUnescapeRegex = Regex("""\\u([0-9a-fA-F]{4})""")

    private val csrfTokenExtractionRegex = Regex("""<meta name="csrf-token" content="(.*?)">""")

    /** [getStudentInfoUnsafe] */
    private val personalEmailExtractionRegex =
        Regex("""StudentContactForm\[email].*?" value="(.*?)"""", RegexOption.MULTILINE)
    private val universityEmailExtractionRegex =
        Regex("""StudentContactForm\[email2].*?" value="(.*?)"""", RegexOption.MULTILINE)
    private val addressExtractionRegex =
        Regex("""StudentContactForm\[address].*?" value="(.*?)"""", RegexOption.MULTILINE)
    private val phoneExtractionRegex =
        Regex("""StudentContactForm\[phone].*?" value="(.*?)"""", RegexOption.MULTILINE)
    private val birthDateExtractionRegex =
        Regex(
            """Birth date .*?<td class="border-none color-black">(.*?)<""",
            RegexOption.MULTILINE
        )
    private val birthYearExtractionRegex =
        Regex(
            """Birthyear .*?<td class="border-none color-black">(.*?)<""",
            RegexOption.MULTILINE
        )
    private val fullNameAndAvatarExtractionRegex =
        Regex("""id="user_img_id".*?alt="(.*?)".*?src="(.*?)"""", RegexOption.MULTILINE)

    /** [getThisSemesterInfoUnsafe] */

    private val currentSemesterInfoExtractionRegex = Regex(
        """<title>(.*?)<""",
        RegexOption.MULTILINE
    )

    private val studySubjectExtractionRegex =
        Regex(
            """<a class="profile-link" href="(.*?)">(.*?)<.*?data-title="Lecturer".*?>(.*?)<.*?data-title="Evaluation".*?>(.*?)<.*?data-title="Credits".*?>(.*?)<""",
            RegexOption.MULTILINE
        )

    /** [getSubjectTimetableUnsafe] */
    private val courseTimetableExtractionRegex =
        Regex(
            """data-title="Work day".*?>(.*?)<.*?data-title="Week".*?>(.*?)<.*?data-title="Time".*?>(.*?)<.*?data-title="Auditorium".*?>(.*?)<.*?data-title="Lecture type".*?>(.*?)<.*?data-title="Lecturer".*?>(.*?)<""",
            RegexOption.MULTILINE
        )

    /** [getExamTimetableUnsafe] */

    private val examTimetableExtractionRegex =
        Regex(
            """data-title="Subject".*?>(.*?)<.*?data-title="Type".*?>(.*?)<.*?data-title="Credits".*?>(.*?)<.*?data-title="Lecturer".*?>(.*?)<.*?data-title="Exam date".*?>(.*?)<.*?data-title="Exam time".*?>(.*?)<.*?class="auditory".*?>(.*?)<.*?>.*?data-title="Consultation date".*?>(.*?)<.*?data-title="Consultation time".*?>(.*?)<.*?data-title="Consultation auditorium".*?>(.*?)<""",
            RegexOption.MULTILINE
        )

    /** [getEmployeesUnsafe] */
    private val outerContactsExtractionRegex = Regex(
        """(<select.*?name="Contacts\[workers]".*?(?:<option.*option>)+)""",
        RegexOption.MULTILINE
    )
    private val innerContactsExtractionRegex = Regex(
        """<option value="(.*?)">(.*?)<.option>""",
        RegexOption.MULTILINE
    )

    /** [getEmployeeDetailsUnsafe] */
    private val employeeNameExtractionRegex = Regex(
        """<div class="employee-name">(.*?)</div>""",
        RegexOption.MULTILINE
    )

    private val employeePositionExtractionRegex = Regex(
        """<div class="employee-position">(.*?)</div>""",
        RegexOption.MULTILINE
    )

    private val employeeDepartmentDataExtractionRegex = Regex(
        """<div class="employee-department" department_id=(.*?)>.*?<a href="#">(.*?)<""",
        RegexOption.MULTILINE
    )

    private val employeePhoneExtractionRegex = Regex(
        """<a href="tel:(.*?)"""",
        RegexOption.MULTILINE
    )

    private val employeeEmailExtractionRegex = Regex(
        """<a href="mailto:(.*?)"""",
        RegexOption.MULTILINE
    )

    private val employeeOfficeExtractionRegex = Regex(
        """<strong>Office</strong>(.*?)<""",
        RegexOption.MULTILINE
    )

    private val employeeAddressExtractionRegex = Regex(
        """<strong>Address</strong>(.*?)<""",
        RegexOption.MULTILINE
    )

    private val employeePhotoExtractionRegex = Regex(
        """src="(.*?)" alt="photo"""",
        RegexOption.MULTILINE
    )

    /** [getCompletedSemesterResultsUnsafe] */

    private val tableBodyExtractionRegex = Regex(
        """<tbody(.*?)tbody>"""
    )

    private val semesterResultsExtractionRegex = Regex(
        """data-title="Name".*?<a href="(.*?)">(.*?)<.*?data-title="Teacher".*?>(.*?)<.*?data-title="Credits".*?>(.*?)<.*?data-title="Hours".*?>(.*?)<.*?data-title="Grade".*?>(.*?)<.*?data-title="Tries".*?>(.*?)<.*?data-title="Date".*?>(.*?)<""",
        RegexOption.MULTILINE
    )

    private val semesterHeaderExtractionRegex = Regex(
        """class="page-header">(.*?)<br>(.*?)group""",
        RegexOption.MULTILINE
    )

    private val semesterFooterExtractionRegex = Regex(
        """Total number of credits.*?><.*?>(.*?)<.*?The weighted grade point average.*?">(.*?)<""",
        RegexOption.MULTILINE
    )

    /** [getSubjectSettlementGroupsUnsafe] */

    private val mediateResultsExtractionRegex = Regex(
        """data-title="Settlement".*?data-years="(.*?)".*?data-sem="(.*?)".*?data-mod-id="(.*?)".*?data-kmd-id="(.*?)".*?data-dest-vart="(.*?)".*?data-type="(.*?)".*?data-name="(.*?)".*?href="#">.*?data-title="Lecturer".*?href="#">(.*?)<.*?title="Completed".*?href="#">(.*?)<.*?title="Percentage of final assessment grade.*?href="#">(.*?)<.*?title="Grade".*?href="#">(.*?)<.*?title="The cumulative score".*?href="#">(.*?)<.*?title="Date".*?href="#">(.*?)<""",
        RegexOption.MULTILINE
    )

    /** [getSettlementGradesUnsafe] */

    private val settlementGradesExtractionRegex = Regex(
        """data-title="Settlement".*?>(.*?)<.*?data-title="Grade".*?>(.*?)<.*?data-title="Date".*?>(.*?)<.*?>(.*?)<""",
        RegexOption.MULTILINE
    )

    /** [getCalloutsUnsafe] */

    private val calloutExtractionRegex = Regex(
        """<div class="callout callout-(.*?)">(.*?)</div>""",
        RegexOption.MULTILINE
    )

    @Volatile
    private var csrfToken = ""

    fun HttpMessageBuilder.addCsrfHeaders() {
        headers.append("X-CSRF-Token", csrfToken)
        headers.append("X-Requested-With", "XMLHttpRequest")
    }

    suspend fun loginIfNeeded(
        studentId: String,
        password: String,
        mfaCode: String
    ): NetResult<String> {
        return VTBaseApi.loginIfNeeded(baseUrl, studentId, password, mfaCode, "login into mano")
    }

    val client = getHttpClientBase().config {
        install(HttpCookies) {
            storage = VTBaseApi.cookieStorage
        }
    }

    private fun String.unescape(): String {
        // https://stackoverflow.com/questions/66264361/unescape-and-get-unicode-string-in-kotlin
        val preCleaned = replace("&amp;", "&")
            .replace("\\\"", "\"")
            .replace("\\n", "\n")
            .replace("\\r", "")
            .replace("&nbsp;", "")
        // Unescape \u0160 and similar
        return unicodeUnescapeRegex.replace(preCleaned) { matchResult ->
            // Get the hex value (e.g., "0160"), convert to Int with base 16, then to Char
            matchResult.groupValues[1].toInt(16).toChar().toString()
        }
    }

    private fun String.singleLine(): String {
        return filterNot { it == '\n' || it == '\r' }
    }

    private fun String.timeToDuration(): Duration {
        val parts = split(":")
        return Duration.parse("${parts[0]}h ${parts[1]}m")
    }

    private fun String.mapLectureType(): ApiManoTimetableEntityType {
        if ("Lecture" in this) {
            return ApiManoTimetableEntityType.LECTURE
        }
        if ("Laboratory" in this) {
            return ApiManoTimetableEntityType.LAB
        }
        return ApiManoTimetableEntityType.PRACTICE
    }

    // Don't allow refreshing the same session multiple times at the same time
    private val sessionRefreshMutex = Mutex()
    suspend fun updateSessionIfNeeded(
        rootOperationName: String,
        prevCallBody: String?
    ): NetResult<String> {
        sessionRefreshMutex.withLock {

            val pageContent =
                getPageWithSamlRefresh(rootOperationName, baseUrl, prevCallBody)
                    .onFailure { return this }
                    .bodyTyped ?: ""

            val extractedCsrfToken = csrfTokenExtractionRegex.findFirstGroup(pageContent)
                ?: return pageContent.toNetResultFail(
                    context = "CSRF token not found",
                    operation = "$rootOperationName + csrf extraction"
                )

            csrfToken = extractedCsrfToken

            return "CSRF token: $extractedCsrfToken".toNetResultOk("$rootOperationName + ret")
        }
    }

    suspend fun getStudentInfo(source: String): NetResult<ApiManoStudentInfo> {
        println("${::getStudentInfo.name} called from $source")
        return safeRetryWithPrecall(
            "get student info", UPDATE_SESSION_OP,
            mainBlock = {
                getStudentInfoUnsafe(it)
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionIfNeeded(op, mainCallResult.bodyRaw)
            })
    }

    private suspend fun getStudentInfoUnsafe(rootOperationName: String): NetResult<ApiManoStudentInfo> {
        val basePageResponse = client.get("$baseUrl/profile/site")

        basePageResponse.expect200<ApiManoStudentInfo>(
            "$rootOperationName + base page request"
        )?.let { return it }

        val contactsPageResponse = client.get("$baseUrl/profile/student/contacts")

        contactsPageResponse.expect200<ApiManoStudentInfo>(
            "$rootOperationName + contacts page request"
        )?.let { return it }

        val basePageResponseContent = basePageResponse.bodyAsText().singleLine()
        val contactsPageResponseContent = contactsPageResponse.bodyAsText().unescape()

        val personalEmail = personalEmailExtractionRegex.findFirstGroup(contactsPageResponseContent)
        val universityEmail =
            universityEmailExtractionRegex.findFirstGroup(contactsPageResponseContent)
        val address = addressExtractionRegex.findFirstGroup(contactsPageResponseContent)
        val phone = phoneExtractionRegex.findFirstGroup(contactsPageResponseContent)

        val birthDate =
            birthDateExtractionRegex.findFirstGroup(basePageResponseContent)?.replace("d.", "")
                ?.trim()
        val birthYear =
            birthYearExtractionRegex.findFirstGroup(basePageResponseContent)?.replace("m.", "")
                ?.trim()
                ?.toInt()

        val fullNameAndAvatarMatch =
            fullNameAndAvatarExtractionRegex.find(basePageResponseContent)?.groupValues
        val fullName = fullNameAndAvatarMatch?.get(1)?.trim()
        var avatarUrl = fullNameAndAvatarMatch?.get(2)?.trim()

        if (!avatarUrl.isNullOrBlank()) {
            avatarUrl = "$baseUrl$avatarUrl"
        }

        return ApiManoStudentInfo(
            fullName = fullName,
            birthYear = birthYear,
            birthDate = birthDate,
            address = address,
            phone = phone,
            personalEmail = personalEmail,
            universityEmail = universityEmail,
            avatarUrl = avatarUrl
        ).toNetResultOk("$rootOperationName + ret")
    }

    suspend fun getThisSemesterInfo(source: String): NetResult<ApiManoThisSemesterInfo> {
        println("${::getThisSemesterInfo.name} called from $source")
        return safeRetryWithPrecall(
            "get current semester info", UPDATE_SESSION_OP,
            mainBlock = {
                getThisSemesterInfoUnsafe(it)
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionIfNeeded(op, mainCallResult.bodyRaw)
            })
    }

    private suspend fun getThisSemesterInfoUnsafe(rootOperationName: String): NetResult<ApiManoThisSemesterInfo> {
        val studySubjectsResponse = client.get("$baseUrl/thissemester/site/studysubject")

        studySubjectsResponse.expect200<ApiManoThisSemesterInfo>(
            "$rootOperationName + studysubject req"
        )?.let { return it }

        val studySubjectsPageIndexResponse = client.get("$baseUrl/thissemester/site/loadindex")

        studySubjectsPageIndexResponse.expect200<ApiManoThisSemesterInfo>(
            "$rootOperationName + loadindex req"
        )?.let { return it }

        val currentSemesterInfo = currentSemesterInfoExtractionRegex.findFirstGroup(
            studySubjectsPageIndexResponse.bodyAsText()
        )
            ?.split("&lt;br&gt;") // Study programme: Information Technologies&lt;br&gt;8 Semester - 4th year (Group: ITVfu-22)

        val studyProgram = currentSemesterInfo?.get(0)?.replace("Study programme:", "")?.trim()
        val semesterAndGroup = currentSemesterInfo?.get(1)?.split("-", limit = 2)

        val semesterAbsoluteSequenceNum =
            semesterAndGroup?.get(0)
                ?.replace("Semester", "")?.trim()?.toInt()
        val group =
            semesterAndGroup?.get(1)?.trim()?.split(" ")?.last()?.trimEnd { it == ' ' || it == ')' }

        val subjects = studySubjectExtractionRegex
            .findAll(
                studySubjectsResponse.bodyAsText().unescape().singleLine()
            ).map { result ->
                val url = Url(result.groupValues[1])
                val modCode = url.parameters["MOD_CODE"] ?: ""
                ApiManoThisSemesterSubjectEntity(
                    modId = url.parameters["MOD_ID"]?.toInt() ?: 0,
                    modCode = modCode,
                    link = result.groupValues[1],
                    name = result.groupValues[2].replace(modCode, "").trim(),
                    lecturerFullName = result.groupValues[3].nullIfDash(),
                    evaluationType = result.groupValues[4],
                    credits = result.groupValues[5].toInt(),
                )
            }.toList()

        return ApiManoThisSemesterInfo(
            studyProgram = studyProgram ?: "",
            absoluteSequenceNum = semesterAbsoluteSequenceNum ?: 0,
            group = group ?: "",
            subjects = subjects
        ).toNetResultOk("$rootOperationName + ret")
    }

    suspend fun getSubjectTimetable(
        source: String,
        subjectModId: String
    ): NetResult<List<ApiManoCourseTimetableEntity>> {
        println("${::getSubjectTimetable.name} called from $source")
        return safeRetryWithPrecall(
            "get subject timetable for '$subjectModId'", UPDATE_SESSION_OP,
            mainBlock = {
                getSubjectTimetableUnsafe(it, subjectModId)
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionIfNeeded(op, mainCallResult.bodyRaw)
            })
    }

    private suspend fun getSubjectTimetableUnsafe(
        rootOperationName: String,
        subjectModId: String
    ): NetResult<List<ApiManoCourseTimetableEntity>> {
        val courseTimetablePageResponse =
            client.get("$baseUrl/thissemester/site/tab-timetable?MOD_ID=$subjectModId")

        courseTimetablePageResponse.expect200<List<ApiManoCourseTimetableEntity>>(
            "$rootOperationName + main request"
        )?.let { return it }

        val matches = courseTimetableExtractionRegex.findAll(
            courseTimetablePageResponse.bodyAsText().singleLine()
        )

        val subjects = matches.map { result ->
            val times = result.groupValues[3].split("-")
            ApiManoCourseTimetableEntity(
                weekDay = ManoTimetableWeekday.valueOf(result.groupValues[1]),
                week = ApiManoTimetableEntityWeek.valueOf(result.groupValues[2]),
                startTime = times[0].timeToDuration(),
                endTime = times[1].timeToDuration(),
                auditorium = result.groupValues[4].trim(),
                type = result.groupValues[5].mapLectureType(),
                lecturerFullName = result.groupValues[6].trim()
            )
        }.toList()

        return subjects.toNetResultOk("$rootOperationName + ret")
    }

    suspend fun getExamTimetable(
        source: String,
        groupName: String,
        semesterAbsoluteSequenceNum: Int
    ): NetResult<List<ApiManoSubjectExamInfo>> {
        println("${::getExamTimetable.name} called from $source")
        return safeRetryWithPrecall(
            "get exam timetable for '$groupName' and semester number $semesterAbsoluteSequenceNum", UPDATE_SESSION_OP,
            mainBlock = {
                getExamTimetableUnsafe(it, groupName, semesterAbsoluteSequenceNum)
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionIfNeeded(op, mainCallResult.bodyRaw)
            })
    }

    private suspend fun getExamTimetableUnsafe(
        rootOperationName: String,
        groupName: String,
        semesterAbsoluteSequenceNum: Int
    ): NetResult<List<ApiManoSubjectExamInfo>> {

        val basePageResponse = client.get("$baseUrl/examstimetable/exams/student")

        basePageResponse.expect200<List<ApiManoSubjectExamInfo>>(
            "$rootOperationName + main request"
        )?.let { return it }

        val basePageContent = basePageResponse.bodyAsText().unescape().singleLine()

        val groupOptionIndex = basePageContent.indexOf("$groupName</option>", ignoreCase = true)

        if (groupOptionIndex == -1) {
            return "Could not find group option index"
                .toNetResultFail(
                    "group option index search",
                    "$rootOperationName + find group"
                )
        }

        // <option value="7287890">ITVf-23</option>
        val fullOption = basePageContent.substring(groupOptionIndex - 25, groupOptionIndex)
        val groupId = fullOption.split('"')[1]

        val examSchedulePageResponse = client.submitForm(
            url = "${baseUrl}/examstimetable/exams/get-students-exams-schedule",
            formParameters = parameters {
                append("group-name", groupName)
                append("group-code", groupId)
                append("semester", "$semesterAbsoluteSequenceNum")
            }
        ) { addCsrfHeaders() }

        examSchedulePageResponse.expect200<List<ApiManoSubjectExamInfo>>(
            "$rootOperationName + timetable request"
        )?.let { return it }

        val examSchedulePageContent = examSchedulePageResponse.bodyAsText().unescape().singleLine()
        val systemTimezone = TimeZone.currentSystemDefault()

        val exams = examTimetableExtractionRegex
            .findAll(examSchedulePageContent)
            .map {
                val modCodeAndName = it.groupValues[1].trim().split(" ", limit = 2)
                val dateOnly = Instant.parse("${it.groupValues[5]}T14:00:00Z")
                val dateTime = Instant.parse(
                    "${it.groupValues[5].trim()}T${it.groupValues[6].trim()}:00+0${
                        systemTimezone.offsetAt(dateOnly).totalSeconds.floorDiv(3600)
                    }:00"
                )
                ApiManoSubjectExamInfo(
                    subjectName = modCodeAndName[1].trim(),
                    subjectModCode = modCodeAndName[0].trim(),
                    examType = it.groupValues[2].trim(),
                    examClassroom = it.groupValues[7].trim(),
                    examDateTime = dateTime,
                    examCredits = it.groupValues[3].trim().toInt(),
                    examLecturerFullName = it.groupValues[4].trim(),
                    consultationClassroom = it.groupValues[10].trim(),
                    consultationDateTime = null // TODO: Extract and use
                )
            }
            .toList()

        return exams.toNetResultOk("$rootOperationName + ret")
    }

    suspend fun getEmployees(source: String): NetResult<List<ApiManoEmployeeBasicEntity>> {
        println("${::getEmployees.name} called from $source")
        return safeRetryWithPrecall(
            "get employees", UPDATE_SESSION_OP,
            mainBlock = {
                getEmployeesUnsafe(it)
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionIfNeeded(op, mainCallResult.bodyRaw)
            })
    }

    private suspend fun getEmployeesUnsafe(rootOperationName: String): NetResult<List<ApiManoEmployeeBasicEntity>> {
        val contactsPageResponse =
            client.get("$baseUrl/contacts/contacts")

        contactsPageResponse.expect200<List<ApiManoEmployeeBasicEntity>>(
            "$rootOperationName + main request"
        )?.let { return it }

        val contactsPageContent = contactsPageResponse.bodyAsText()

        val outerMatch = outerContactsExtractionRegex.findFirstGroup(
            contactsPageContent.singleLine()
        )

        if (outerMatch == null) {
            return contactsPageContent.toNetResultFail(
                context = "outerMatch == null",
                operation = "$rootOperationName + outer contacts extraction"
            )
        }

        val matches = innerContactsExtractionRegex.findAll(outerMatch)

        val employees = matches.mapNotNull { result ->
            val id = result.groupValues[1].trim().toLongOrNull() ?: return@mapNotNull null
            return@mapNotNull ApiManoEmployeeBasicEntity(
                id = id,
                shortName = result.groupValues[2].trim()
            )
        }.toList()

        return employees.toNetResultOk("$rootOperationName + ret")
    }

    suspend fun getEmployeeDetails(
        source: String,
        employeeId: Long
    ): NetResult<ApiManoEmployeeDetails> {
        println("${::getEmployeeDetails.name} called from $source")
        return safeRetryWithPrecall(
            "get employee details for '$employeeId'", UPDATE_SESSION_OP,
            mainBlock = {
                getEmployeeDetailsUnsafe(it, employeeId)
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionIfNeeded(op, mainCallResult.bodyRaw)
            })
    }

    private suspend fun getEmployeeDetailsUnsafe(
        rootOperationName: String,
        employeeId: Long
    ): NetResult<ApiManoEmployeeDetails> {
        val detailsPageResponse =
            client.get("$baseUrl/contacts/contacts/employee-data?id=$employeeId")

        detailsPageResponse.expect200<ApiManoEmployeeDetails>(
            "$rootOperationName + main request"
        )?.let { return it }

        val detailsPageContent = detailsPageResponse.bodyAsText().singleLine()

        val fullName = employeeNameExtractionRegex.findFirstGroup(detailsPageContent)

        if (fullName.isNullOrBlank()) {
            return "Full name is required"
                .toNetResultFail(
                    "extraction precheck",
                    "$rootOperationName + extraction"
                )
        }

        val phones = employeePhoneExtractionRegex
            .findAll(detailsPageContent)
            .map { result ->
                result.groupValues[1].trim()
            }
            .distinct()
            .toList()

        val emails = employeeEmailExtractionRegex
            .findAll(detailsPageContent)
            .map { result ->
                result.groupValues[1].trim()
            }
            .distinct()
            .toList()

        val departments = employeeDepartmentDataExtractionRegex
            .findAll(detailsPageContent)
            .map { result ->
                ApiManoBasicDepartmentData(
                    id = result.groupValues[1].trim(),
                    name = result.groupValues[2].trim()
                )
            }
            .distinct()
            .toList()

        val employeeOffices = employeeOfficeExtractionRegex
            .findAll(detailsPageContent)
            .map { result -> result.groupValues[1].trim() }
            .toList()

        val employeeAddresses = employeeAddressExtractionRegex
            .findAll(detailsPageContent)
            .map { result -> result.groupValues[1].trim() }
            .toList()

        var employeePhotoUrl = employeePhotoExtractionRegex
            .findFirstGroup(detailsPageContent) ?: ""

        // Relative to absolute url if it's not empty
        if (!employeePhotoUrl.isBlank() && !employeePhotoUrl.startsWith("http")) {
            employeePhotoUrl = "$baseUrl$employeePhotoUrl"
        }

        if (employeeOffices.size != employeeAddresses.size) {
            return detailsPageContent
                .toNetResultFail(
                    "employeeOffices.size != employeeAddresses.size",
                    "$rootOperationName + extract offices"
                )
        }

        val offices =
            employeeOffices
                .fastZip(employeeAddresses)
                { office, address ->
                    ApiManoBasicOfficeData(
                        officeName = office,
                        address = address,
                    )
                }
                .distinct()
                .toList()

        val positions = employeePositionExtractionRegex
            .findAll(detailsPageContent)
            .map { result ->
                result.groupValues[1].trim()
            }
            .distinct()
            .toList()

        return ApiManoEmployeeDetails(
            phones = phones,
            emails = emails,
            departments = departments,
            offices = offices,
            positions = positions,
            fullNameWithPrefix = fullName,
            avatarUrl = employeePhotoUrl.nullIfBlank()
        ).toNetResultOk("$rootOperationName + ret")
    }

    suspend fun getCompletedSemesterResults(source: String): NetResult<List<ApiManoCompletedSemesterResult>> {
        println("${::getCompletedSemesterResults.name} called from $source")
        return safeRetryWithPrecall(
            "get completed semester results", UPDATE_SESSION_OP,
            mainBlock = {
                getCompletedSemesterResultsUnsafe(it)
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionIfNeeded(op, mainCallResult.bodyRaw)
            })
    }

    private suspend fun getCompletedSemesterResultsUnsafe(
        rootOperationName: String
    ): NetResult<List<ApiManoCompletedSemesterResult>> {
        val resultsResponse =
            client.get("$baseUrl/results/site/my-results")

        resultsResponse.expect200<List<ApiManoCompletedSemesterResult>>(
            "$rootOperationName + main request"
        )?.let { return it }

        val resultsContent = resultsResponse.bodyAsText().unescape().singleLine()

        val semesterHeaders = semesterHeaderExtractionRegex.findAll(resultsContent).toList()

        val allTables = tableBodyExtractionRegex.findAll(resultsContent)
        val mainContentTables = allTables.filterIndexed { index, _ -> index % 2 == 0 }.toList()
        val footerContentTables = allTables.filterIndexed { index, _ -> index % 2 == 1 }.toList()

        if (semesterHeaders.size != mainContentTables.size || mainContentTables.size != footerContentTables.size) {
            return resultsContent.toNetResultFail(
                "Section size mismatch (sem: ${semesterHeaders.size} main: ${mainContentTables.size} footer: ${footerContentTables.size})",
                "$rootOperationName + extract results"
            )
        }

        val semesterResults = semesterHeaders
            .fastZip(footerContentTables) { semHeader, semFooter ->
                // 2025-2026 academic year, winter session, 7 semester
                // ITVfu-22 group
                val semHeaderParts = semHeader.groupValues[1].split(",").fastMap { it.trim() }

                val semFooterParts = semesterFooterExtractionRegex.find(semFooter.groupValues[1])

                ApiManoCompletedSemesterResult(
                    absoluteSequenceNum = semHeaderParts[2].replace("semester", "").trim()
                        .toInt(),
                    sessionSeason = semHeaderParts[1],
                    group = semHeader.groupValues[2],
                    yearTimeSpan = semHeaderParts[0].replace("academic year", "").trim(),
                    finalTotalCredits = semFooterParts?.groupValues[1]?.toInt() ?: 0,
                    finalWeightedGrade = semFooterParts?.groupValues[2]?.toFloatDashAsNull() ?: 0F,
                    finalResults = mutableListOf()
                )
            }
            .fastZip(mainContentTables) { result, mainContent ->
                val semesterResults =
                    semesterResultsExtractionRegex
                        .findAll(mainContent.groupValues[1])
                        .map { result ->
                            val url = Url(result.groupValues[1])

                            // The grade column may contain either a grade or a general verdict, try to parse both ways
                            val verdictOrGrade = result.groupValues[6]
                            var grade = 10
                            var verdict = ApiManoSubjectEvaluationVerdict.PASS

                            try {
                                grade = verdictOrGrade.toInt()
                                if (grade <= 5) {
                                    verdict = ApiManoSubjectEvaluationVerdict.FAIL
                                }
                            } catch (_: NumberFormatException) {
                                verdict = EvaluationVerdictLookup[verdictOrGrade] ?: verdict
                                grade =
                                    if (verdict == ApiManoSubjectEvaluationVerdict.PASS)
                                        10
                                    else
                                        0
                            }

                            val modCode = url.parameters["MOD_CODE"] ?: ""

                            ApiManoSubjectFinalResult(
                                modId = url.parameters["MOD_ID"]?.toInt() ?: 0,
                                modCode = modCode,
                                link = result.groupValues[1],
                                name = result.groupValues[2]
                                    .replace(modCode, "")
                                    .trim { it == ' ' || it == '(' || it == ')' },
                                lecturerShortName = result.groupValues[3].nullIfDash(),
                                credits = result.groupValues[4].toIntNotNull(),
                                hours = result.groupValues[5].toIntNotNull(),
                                evaluationVerdict = verdict,
                                grade = grade,
                                tries = result.groupValues[7].toIntNotNull(),
                                completionDate = result.groupValues[8]
                            )
                        }
                result.finalResults.addAll(semesterResults)
                return@fastZip result
            }

        return semesterResults.toNetResultOk("$rootOperationName + ret")
    }

    suspend fun getSemesterMediateResults(
        source: String,
        semesterAbsoluteSequenceNum: Int,
        semesterYearRange: String
    ): NetResult<ApiManoSemesterMediateResults> {
        println("${::getSemesterMediateResults.name} called from $source")
        return safeRetryWithPrecall(
            "get mediate results for semester '$semesterAbsoluteSequenceNum' in '$semesterYearRange'",
            UPDATE_SESSION_OP,
            mainBlock = {
                getSemesterMediateResultsUnsafe(semesterAbsoluteSequenceNum, semesterYearRange, it)
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionIfNeeded(op, mainCallResult.bodyRaw)
            })
    }

    private suspend fun getSemesterMediateResultsUnsafe(
        semesterAbsoluteSequenceNum: Int,
        semesterYearRange: String,
        rootOperationName: String
    ): NetResult<ApiManoSemesterMediateResults> {

        val semesterResultDetailsResponse = client.submitForm(
            url = "${baseUrl}/results/site/get-result-part",
            formParameters = parameters {
                append("years", semesterYearRange)
                append(
                    "sem",
                    "${semesterAbsoluteSequenceNum.toRelativeSemester()}"
                ) // 1 - N to 1 or 2
            }
        ) { addCsrfHeaders() }

        semesterResultDetailsResponse.expect200<ApiManoSemesterMediateResults>(
            "$rootOperationName + main request"
        )?.let { return it }

        return jsonSerializer.decodeFromString<ApiManoSemesterMediateResults>(
            semesterResultDetailsResponse.bodyAsText()
        )
            .toNetResultOk("$rootOperationName + ret")
    }

    suspend fun getSubjectSettlementGroups(
        source: String,
        semesterAbsoluteSequenceNum: Int,
        semesterYearRange: String,
        subjectModId: Int,
    ): NetResult<List<ApiManoSubjectSettlementOverview>> {
        println("${::getSubjectSettlementGroups.name} called from $source")
        return safeRetryWithPrecall(
            "get settlement groups for subject (mod id) '$subjectModId'",
            UPDATE_SESSION_OP,
            mainBlock = {
                getSubjectSettlementGroupsUnsafe(
                    semesterAbsoluteSequenceNum,
                    semesterYearRange,
                    subjectModId,
                    it
                )
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionIfNeeded(op, mainCallResult.bodyRaw)
            })
    }


    private suspend fun getSubjectSettlementGroupsUnsafe(
        semesterAbsoluteSequenceNum: Int,
        semesterYearRange: String,
        subjectModId: Int,
        rootOperationName: String
    ): NetResult<List<ApiManoSubjectSettlementOverview>> {

        val settlementGroupsResponse = client.submitForm(
            url = "${baseUrl}/results/site/get-mediate",
            formParameters = parameters {
                append("years", semesterYearRange)
                append(
                    "sem",
                    "${semesterAbsoluteSequenceNum.toRelativeSemester()}"
                ) // 1 - N to 1 or 2
                append("mod", "$subjectModId")
            }
        ) { addCsrfHeaders() }

        settlementGroupsResponse.expect200<List<ApiManoSubjectSettlementOverview>>(
            "$rootOperationName + main request"
        )?.let { return it }

        val settlementGroupsContent =
            settlementGroupsResponse.bodyAsText().unescape().singleLine()

        val groups = mediateResultsExtractionRegex
            .findAll(settlementGroupsContent)
            .map { result ->
                val values = result.groupValues
                ApiManoSubjectSettlementOverview(
                    semesterYearRange = values[1],
                    semesterRelativeSequenceNum = values[2].toInt(),
                    subjectModId = values[3],
                    subjectKmdId = values[4],
                    subjectDestVart = values[5],
                    settlementType = values[6],
                    subjectName = values[7],
                    lecturerName = values[8],
                    completedRatio = values[9],
                    percentageOfFinalAssessment = values[10].replace("%", "").toIntNotNull(),
                    finalGrade = values[11].toFloatDashAsNull(),
                    finalCumulativeScore = values[12].toFloatDashAsNull(),
                    lastUpdatedDate = values[13]
                )
            }.toList()

        return groups.toNetResultOk("$rootOperationName + ret")
    }

    suspend fun getSettlementGrades(
        source: String,
        semesterRelativeSequenceNum: Int,
        subjectModId: Int,
        subjectKmdId: String,
        subjectName: String,
        subjectDestVart: String,
        semesterYearRange: String,
        settlementType: String,
    ): NetResult<List<ApiManoSubjectSettlementGrade>> {
        println("${::getSettlementGrades.name} called from $source")
        return safeRetryWithPrecall(
            "get grades for '$settlementType', '$subjectName'",
            UPDATE_SESSION_OP,
            mainBlock = {
                getSettlementGradesUnsafe(
                    semesterRelativeSequenceNum,
                    subjectModId,
                    subjectKmdId,
                    subjectName,
                    subjectDestVart,
                    semesterYearRange,
                    settlementType,
                    it
                )
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionIfNeeded(op, mainCallResult.bodyRaw)
            })
    }

    private suspend fun getSettlementGradesUnsafe(
        semesterRelativeSequenceNum: Int,
        subjectModId: Int,
        subjectKmdId: String,
        subjectName: String,
        subjectDestVart: String,
        semesterYearRange: String,
        settlementType: String,
        rootOperationName: String
    ): NetResult<List<ApiManoSubjectSettlementGrade>> {

        val settlementGradesResponse = client.submitForm(
            url = "${baseUrl}/results/site/get-mediate-details",
            formParameters = parameters {
                append("result_data[sem_id]", "$semesterRelativeSequenceNum")
                append("result_data[mod_id]", "$subjectModId")
                append("result_data[kmd_id]", subjectKmdId)
                append("result_data[mod_name]", subjectName)
                append("result_data[dest-vart]", subjectDestVart)
                append("result_data[years]", semesterYearRange)
                append("result_data[type]", settlementType)
            }
        ) { addCsrfHeaders() }

        settlementGradesResponse.expect200<List<ApiManoSubjectSettlementGrade>>(
            "$rootOperationName + main request"
        )?.let { return it }

        val settlementGradesContent = settlementGradesResponse.bodyAsText().unescape().singleLine()

        val grades = settlementGradesExtractionRegex
            .findAll(settlementGradesContent)
            .map { result ->
                with(result.groupValues) {
                    ApiManoSubjectSettlementGrade(
                        name = get(1),
                        grade = get(2).toIntDashAsNull(),
                        gradeDate = get(3).nullIfDash(),
                        graderName = get(4).nullIfDash()
                    )
                }
            }.toList()

        return grades.toNetResultOk("$rootOperationName + ret")
    }

    suspend fun getCallouts(source: String): NetResult<List<ApiManoCalloutData>> {
        println("${::getCallouts.name} called from $source")
        return safeRetryWithPrecall(
            "get callouts", UPDATE_SESSION_OP,
            mainBlock = {
                getCalloutsUnsafe(it)
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionIfNeeded(op, mainCallResult.bodyRaw)
            })
    }

    private suspend fun getCalloutsUnsafe(rootOperationName: String): NetResult<List<ApiManoCalloutData>> {

        val basePageResponse = client.get(baseUrl)

        basePageResponse.expect200<List<ApiManoCalloutData>>(
            "$rootOperationName + main request"
        )?.let { return it }

        val pageContent = basePageResponse.bodyAsText().singleLine()

        if (!pageContent.contains("class=\"navbar navbar-static-top\"")) {
            return "Page must be home page"
                .toNetResultFail(
                    "extraction precheck",
                    "$rootOperationName + extraction"
                )
        }

        val callouts = calloutExtractionRegex.findAll(pageContent)
            .mapNotNull {
                val type = it.groupValues[1]
                val content = it.groupValues[2];
                if (type.isBlank()
                    || content.contains("Your web browser") // Filter out placeholder callouts
                    || content.contains("The student is currently")
                ) {
                    return@mapNotNull null
                }
                return@mapNotNull ApiManoCalloutData(
                    type = type,
                    contents = content
                )
            }
            .toList()

        return callouts.toNetResultOk("$rootOperationName + ret")
    }
}