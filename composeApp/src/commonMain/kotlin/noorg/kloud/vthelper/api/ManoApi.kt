package noorg.kloud.vthelper.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.io.Buffer
import kotlinx.io.files.SystemFileSystem
import noorg.kloud.vthelper.api.models.NetResult
import noorg.kloud.vthelper.api.models.expect200
import noorg.kloud.vthelper.api.models.mano.ApiManoBasicDepartmentData
import noorg.kloud.vthelper.api.models.mano.ApiManoBasicOfficeData
import noorg.kloud.vthelper.api.models.mano.ApiManoStudentInfo
import noorg.kloud.vthelper.api.models.mano.ApiManoCourseEntity
import noorg.kloud.vthelper.api.models.mano.ApiManoCourseTimetableEntity
import noorg.kloud.vthelper.api.models.mano.ApiManoEmployeeBasicEntity
import noorg.kloud.vthelper.api.models.mano.ApiManoEmployeeDetails
import noorg.kloud.vthelper.api.models.mano.ApiManoTimetableEntityType
import noorg.kloud.vthelper.api.models.mano.ApiManoTimetableEntityWeek
import noorg.kloud.vthelper.api.models.mano.ManoTimetableWeekday
import noorg.kloud.vthelper.findFirstGroup
import noorg.kloud.vthelper.platform_specific.appDataDirectory
import noorg.kloud.vthelper.platform_specific.div
import noorg.kloud.vthelper.platform_specific.getHttpClientEngine
import kotlin.time.Duration

object ManoApi {

    val baseUrl = Url("https://mano.vilniustech.lt/")

    /** [getStudentInfoUnsafe] */
    val personalEmailExtractionRegex =
        Regex("""StudentContactForm\[email].*?" value="(.*?)"""", RegexOption.MULTILINE)
    val universityEmailExtractionRegex =
        Regex("""StudentContactForm\[email2].*?" value="(.*?)"""", RegexOption.MULTILINE)
    val addressExtractionRegex =
        Regex("""StudentContactForm\[address].*?" value="(.*?)"""", RegexOption.MULTILINE)
    val phoneExtractionRegex =
        Regex("""StudentContactForm\[phone].*?" value="(.*?)"""", RegexOption.MULTILINE)
    val birthDateExtractionRegex =
        Regex(
            """Birth date .*?<td class="border-none color-black">(.*?)<""",
            RegexOption.MULTILINE
        )
    val birthYearExtractionRegex =
        Regex(
            """Birthyear .*?<td class="border-none color-black">(.*?)<""",
            RegexOption.MULTILINE
        )
    val fullNameAndAvatarExtractionRegex =
        Regex("""id="user_img_id".*?alt="(.*?)".*?src="(.*?)"""", RegexOption.MULTILINE)

    /** [getThisSemesterSubjectsUnsafe] */
    val studySubjectExtractionRegex =
        Regex(
            """<a class="profile-link" href="(.*?)">(.*?)<.*?data-title="Lecturer".*?>(.*?)<.*?data-title="Evaluation".*?>(.*?)<.*?data-title="Credits".*?>(.*?)<""",
            RegexOption.MULTILINE
        )

    /** [getSubjectTimetableUnsafe] */
    val courseTimetableExtractionRegex =
        Regex(
            """data-title="Work day".*?>(.*?)<.*?data-title="Week".*?>(.*?)<.*?data-title="Time".*?>(.*?)<.*?data-title="Auditorium".*?>(.*?)<.*?data-title="Lecture type".*?>(.*?)<.*?data-title="Lecturer".*?>(.*?)<""",
            RegexOption.MULTILINE
        )

    /** [getEmployeesUnsafe] */
    val outerContactsExtractionRegex = Regex(
        """<div class=".*?loading-contacts-workers".*?<select.*?\n(?:<option.*option>\n)+""",
        RegexOption.MULTILINE
    )
    val innerContactsExtractionRegex = Regex(
        """<option value="(.*?)">(.*?)<.option>""",
        RegexOption.MULTILINE
    )

    /** [getEmployeeDetailsUnsafe] */
    var employeeNameExtractionRegex = Regex(
        """<div class="employee-name">(.*?)<\\/div>""",
        RegexOption.MULTILINE
    )

    var employeePositionExtractionRegex = Regex(
        """<div class="employee-position">(.*?)<\\/div>""",
        RegexOption.MULTILINE
    )

    var employeeDepartmentDataExtractionRegex = Regex(
        """<div class="employee-department" department_id=(.*?)>.*?<a href="#">(.*?)<""",
        RegexOption.MULTILINE
    )

    var employeePhoneExtractionRegex = Regex(
        """<a href="tel:(.*?)"""",
        RegexOption.MULTILINE
    )

    var employeeEmailExtractionRegex = Regex(
        """<a href="mailto:(.*?)"""",
        RegexOption.MULTILINE
    )

    var employeeOfficeExtractionRegex = Regex(
        """<strong>Office<\\/strong>(.*?)<""",
        RegexOption.MULTILINE
    )

    var employeeAddressExtractionRegex = Regex(
        """<strong>Address<\\/strong>(.*?)<""",
        RegexOption.MULTILINE
    )

    suspend fun loginIfNeeded(
        studentId: String,
        password: String,
        mfaCode: String
    ): NetResult<String> {
        return VTBaseApi.loginIfNeeded(baseUrl, studentId, password, mfaCode, "login into mano")
    }

    val client = HttpClient(getHttpClientEngine()) {
        install(HttpCookies) {
            storage = VTBaseApi.cookieStorage
        }
    }

    private fun String.cleanHttpResponse(): String {
        return replace("&amp;", "&")
            .replace("\\\"", "\"")
            .replace("\\/", "/")
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

    suspend fun getStudentInfo(): NetResult<ApiManoStudentInfo> {
        return safeNetCall("get student info") {
            getStudentInfoUnsafe(it)
        }
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
        val contactsPageResponseContent = contactsPageResponse.bodyAsText().cleanHttpResponse()

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
                ?.trim()?.toInt()

        val fullNameAndAvatarMatch =
            fullNameAndAvatarExtractionRegex.find(basePageResponseContent)?.groupValues
        val fullName = fullNameAndAvatarMatch?.get(1)?.trim()
        val avatarUrl = fullNameAndAvatarMatch?.get(2)?.trim()

        return NetResult.fromDeserializedModel(
            ApiManoStudentInfo(
                fullName = fullName ?: "",
                birthYear = birthYear ?: 0,
                birthDate = birthDate ?: "",
                address = address ?: "",
                phone = phone ?: "",
                personalEmail = personalEmail ?: "",
                universityEmail = universityEmail ?: "",
                avatarUrl = "$baseUrl$avatarUrl"
            ), operation = rootOperationName
        )
    }

    suspend fun getThisSemesterSubjects(): NetResult<List<ApiManoCourseEntity>> {
        return safeNetCall("get subjects for current semester") {
            getThisSemesterSubjectsUnsafe(it)
        }
    }

    private suspend fun getThisSemesterSubjectsUnsafe(rootOperationName: String): NetResult<List<ApiManoCourseEntity>> {
        val studySubjectsPageResponse = client.get("$baseUrl/thissemester/site/studysubject")

        studySubjectsPageResponse.expect200<List<ApiManoCourseEntity>>(
            "$rootOperationName + main request"
        )?.let { return it }

        val matches = studySubjectExtractionRegex.findAll(
            studySubjectsPageResponse.bodyAsText().cleanHttpResponse()
        )

        val subjects = matches.map { result ->
            ApiManoCourseEntity(
                subjectModId = Url(result.groupValues[1]).parameters["MOD_ID"]
                    ?: "",
                subjectModCode = Url(result.groupValues[1]).parameters["MOD_CODE"]
                    ?: "",
                subjectLink = result.groupValues[1],
                subjectName = result.groupValues[2],
                subjectLecturerFullName = result.groupValues[3],
                subjectEvaluationCode = result.groupValues[4],
                subjectCredits = result.groupValues[5].toInt(),
            )
        }.toList()

        return NetResult.fromDeserializedModel(subjects, operation = rootOperationName)
    }

    suspend fun getThisSemesterSubjects(courseModId: String): NetResult<List<ApiManoCourseTimetableEntity>> {
        return safeNetCall("get subject timetable for '$courseModId'") {
            getSubjectTimetableUnsafe(it, courseModId)
        }
    }

    private suspend fun getSubjectTimetableUnsafe(
        rootOperationName: String,
        courseModId: String
    ): NetResult<List<ApiManoCourseTimetableEntity>> {
        val courseTimetablePageResponse =
            client.get("$baseUrl/thissemester/site/tab-timetable?MOD_ID=$courseModId")

        courseTimetablePageResponse.expect200<List<ApiManoCourseTimetableEntity>>(
            "$rootOperationName + main request"
        )?.let { return it }

        val matches = courseTimetableExtractionRegex.findAll(
            courseTimetablePageResponse.bodyAsText().cleanHttpResponse()
        )

        val subjects = matches.map { result ->
            ApiManoCourseTimetableEntity(
                weekDay = ManoTimetableWeekday.valueOf(result.groupValues[1]),
                week = ApiManoTimetableEntityWeek.valueOf(result.groupValues[2]),
                startTime = result.groupValues[3].split("-")[0].timeToDuration(),
                endTime = result.groupValues[3].split("-")[1].timeToDuration(),
                auditorium = result.groupValues[4].trim(),
                type = result.groupValues[5].mapLectureType(),
                lecturerFullName = result.groupValues[6].trim()
            )
        }.toList()

        return NetResult.fromDeserializedModel(subjects, operation = rootOperationName)
    }

    suspend fun getEmployees(): NetResult<List<ApiManoEmployeeBasicEntity>> {
        return safeNetCall("get employees") {
            getEmployeesUnsafe(it)
        }
    }

    private suspend fun getEmployeesUnsafe(rootOperationName: String): NetResult<List<ApiManoEmployeeBasicEntity>> {
        val contactsPageResponse =
            client.get("$baseUrl/contacts/contacts")

        contactsPageResponse.expect200<List<ApiManoEmployeeBasicEntity>>(
            "$rootOperationName + main request"
        )?.let { return it }

        val contactsPageContent = contactsPageResponse.bodyAsText()

        val outerMatch = outerContactsExtractionRegex.findFirstGroup(
            contactsPageContent.cleanHttpResponse()
        )

        if (outerMatch == null) {
            return NetResult<List<ApiManoEmployeeBasicEntity>>(
                statusCode = HttpStatusCode.OK,
                bodyRaw = contactsPageContent,
                bodyTyped = null,
                context = "Extraction failed",
                isSuccess = false,
                operation = "$rootOperationName + outer contacts extraction"
            ).logIt()
        }

        val matches = innerContactsExtractionRegex.findAll(outerMatch)

        val employees = matches.map { result ->
            ApiManoEmployeeBasicEntity(
                id = result.groupValues[1].trim(),
                fullName = result.groupValues[2].trim()
            )
        }.toList()

        return NetResult.fromDeserializedModel(employees, operation = rootOperationName)
    }

    suspend fun getEmployeeDetails(employeeId: String): NetResult<ApiManoEmployeeDetails> {
        return safeNetCall("get employee details for '$employeeId'") {
            getEmployeeDetailsUnsafe(it, employeeId)
        }
    }

    private suspend fun getEmployeeDetailsUnsafe(
        rootOperationName: String,
        employeeId: String
    ): NetResult<ApiManoEmployeeDetails> {
        val detailsPageResponse =
            client.get("$baseUrl/contacts/contacts/employee-data?id=$employeeId")

        detailsPageResponse.expect200<ApiManoEmployeeDetails>(
            "$rootOperationName + main request"
        )?.let { return it }

        val detailsPageContent = detailsPageResponse.bodyAsText().singleLine()

        val phones = employeePhoneExtractionRegex.findAll(detailsPageContent).map { result ->
            result.groupValues[1].trim()
        }.distinct().toList()

        val emails = employeeEmailExtractionRegex.findAll(detailsPageContent).map { result ->
            result.groupValues[1].trim()
        }.distinct().toList()

        val departments =
            employeeDepartmentDataExtractionRegex.findAll(detailsPageContent).map { result ->
                ApiManoBasicDepartmentData(
                    id = result.groupValues[1].trim(),
                    name = result.groupValues[2].trim()
                )
            }.distinct().toList()

        val offices = employeeOfficeExtractionRegex
            .findAll(detailsPageContent)
            .map { result ->
                result.groupValues[1].trim()
            }.zip(
                employeeAddressExtractionRegex
                    .findAll(detailsPageContent)
                    .map { result -> result.groupValues[1].trim() })
            .map { result ->
                ApiManoBasicOfficeData(
                    officeName = result.first,
                    address = result.second,
                )
            }.distinct().toList()

        val positions =
            employeePositionExtractionRegex.findAll(detailsPageContent).map { result ->
                result.groupValues[1].trim()
            }.distinct().toList()

        val fullName = employeeNameExtractionRegex.findFirstGroup(detailsPageContent)

        return NetResult.fromDeserializedModel(
            ApiManoEmployeeDetails(
                phones = phones,
                emails = emails,
                departments = departments,
                offices = offices,
                positions = positions,
                fullName = fullName ?: ""
            ), operation = rootOperationName
        )
    }
}