package noorg.kloud.vthelper.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import noorg.kloud.vthelper.api.models.ApiResult
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
import noorg.kloud.vthelper.api.models.mano.toApiResult
import noorg.kloud.vthelper.findFirstGroup
import kotlin.time.Duration

class ManoApi {
    companion object {
        val baseUrl = Url("https://mano.vilniustech.lt/")

        /** [getStudentInfo] */
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
                """Birth date .*\n.*<td class="border-none color-black">\n(.*?)\n""",
                RegexOption.MULTILINE
            )
        val birthYearExtractionRegex =
            Regex(
                """Birthyear .*\n.*<td class="border-none color-black">\n(.*?)\n""",
                RegexOption.MULTILINE
            )
        val fullNameAndAvatarExtractionRegex =
            Regex("""id="user_img_id".*\n.*?alt="(.*?)".*src="(.*?)"""", RegexOption.MULTILINE)

        /** [getThisSemesterSubjects] */
        val studySubjectExtractionRegex =
            Regex(
                """<a class="profile-link" href="(.*?)">(.*?)<.*?data-title="Lecturer".*?>(.*?)<.*?data-title="Evaluation".*?>(.*?)<.*?data-title="Credits".*?>(.*?)<""",
                RegexOption.MULTILINE
            )

        /** [getSubjectTimetable] */
        val courseTimetableExtractionRegex =
            Regex(
                """data-title="Work day".*?>(.*?)<.*?data-title="Week".*?>(.*?)<.*?data-title="Time".*?>(.*?)<.*?data-title="Auditorium".*?>(.*?)<.*?data-title="Lecture type".*?>(.*?)<.*?data-title="Lecturer".*?>(.*?)<""",
                RegexOption.MULTILINE
            )

        /** [getEmployees] */
        val outerContactsExtractionRegex = Regex(
            """<div class=".*?loading-contacts-workers".*?<select.*?\n(?:<option.*option>\n)+""",
            RegexOption.MULTILINE
        )
        val innerContactsExtractionRegex = Regex(
            """<option value="(.*?)">(.*?)<.option>""",
            RegexOption.MULTILINE
        )

        /** [getEmployeeDetails] */
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
    }

    suspend fun loginIfNeeded(
        username: String,
        password: String,
        mfaCode: String
    ): ApiResult<String> {
        return VTBaseApi.loginIfNeeded(baseUrl, username, password, mfaCode)
    }

    val client = HttpClient(CIO) {
        install(HttpCookies) {
            storage = VTBaseApi.cookieStorage
        }
    }

    private fun String.cleanHttpResponse(): String {
        return replace("&amp;", "&")
            .replace("\\\"", "\"")
            .replace("\\/", "/")
    }

    suspend fun getStudentInfo(): ApiResult<ApiManoStudentInfo> {
        val basePageResponse = client.get("$baseUrl/profile/site")

        basePageResponse.expect200<ApiManoStudentInfo>(
            "Get student info (base page)"
        )?.let { return it }

        val contactsPageResponse = client.get("$baseUrl/profile/student/contacts")

        contactsPageResponse.expect200<ApiManoStudentInfo>(
            "Get student info (contacts)"
        )?.let { return it }

        val basePageResponseContent = basePageResponse.bodyAsText().cleanHttpResponse()
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
        val fullName = fullNameAndAvatarMatch?.get(1)
        val avatarUrl = fullNameAndAvatarMatch?.get(2)

        return ApiManoStudentInfo(
            fullName = fullName ?: "",
            birthYear = birthYear ?: 0,
            birthDate = birthDate ?: "",
            address = address ?: "",
            phone = phone ?: "",
            personalEmail = personalEmail ?: "",
            universityEmail = universityEmail ?: "",
            avatarUrl = "$baseUrl$avatarUrl"
        ).toApiResult(operation = "Get student info")
    }

    suspend fun getThisSemesterSubjects(): ApiResult<List<ApiManoCourseEntity>> {
        val op = "Get study subjects"

        val studySubjectsPageResponse = client.get("$baseUrl/thissemester/site/studysubject")

        studySubjectsPageResponse.expect200<List<ApiManoCourseEntity>>(
            op
        )?.let { return it }

        val matches = studySubjectExtractionRegex.findAll(
            studySubjectsPageResponse.bodyAsText().cleanHttpResponse()
        )

        val subjects = matches.map { result ->
            ApiManoCourseEntity(
                subjectModId = Url(result.groupValues[1]).parameters?.get("MOD_ID")
                    ?: "",
                subjectModCode = Url(result.groupValues[1]).parameters?.get("MOD_CODE")
                    ?: "",
                subjectLink = result.groupValues[1],
                subjectName = result.groupValues[2],
                subjectLecturerFullName = result.groupValues[3],
                subjectEvaluationCode = result.groupValues[4],
                subjectCredits = result.groupValues[5].toInt(),
            )
        }.toList()

        return ApiResult.fromDeserializedModel(subjects, operation = op)
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

    suspend fun getSubjectTimetable(courseModId: String): ApiResult<List<ApiManoCourseTimetableEntity>> {
        val op = "Get course timetable $courseModId"

        val courseTimetablePageResponse =
            client.get("$baseUrl/thissemester/site/tab-timetable?MOD_ID=$courseModId")

        courseTimetablePageResponse.expect200<List<ApiManoCourseTimetableEntity>>(
            op
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

        return ApiResult.fromDeserializedModel(subjects, operation = op)
    }

    suspend fun getEmployees(): ApiResult<List<ApiManoEmployeeBasicEntity>> {
        val op = "Get employees"

        val contactsPageResponse =
            client.get("$baseUrl/contacts/contacts")

        contactsPageResponse.expect200<List<ApiManoEmployeeBasicEntity>>(
            op
        )?.let { return it }

        val contactsPageContent = contactsPageResponse.bodyAsText()

        val outerMatch = outerContactsExtractionRegex.findFirstGroup(
            contactsPageContent.cleanHttpResponse()
        )

        if (outerMatch == null) {
            return ApiResult(
                statusCode = HttpStatusCode.OK,
                bodyRaw = contactsPageContent,
                bodyTyped = null,
                context = "Outer contacts extraction failed",
                isSuccessful = false,
                operation = op
            )
        }

        val matches = innerContactsExtractionRegex.findAll(outerMatch)

        val employees = matches.map { result ->
            ApiManoEmployeeBasicEntity(
                id = result.groupValues[1].trim(),
                fullName = result.groupValues[2].trim()
            )
        }.toList()

        return ApiResult.fromDeserializedModel(employees, operation = op)
    }

    suspend fun getEmployeeDetails(employeeId: String): ApiResult<ApiManoEmployeeDetails> {
        val op = "Get employee details for $employeeId"

        val detailsPageResponse =
            client.get("$baseUrl/contacts/contacts/employee-data?id=$employeeId")

        detailsPageResponse.expect200<ApiManoEmployeeDetails>(
            op
        )?.let { return it }

        val detailsPageContent = detailsPageResponse.bodyAsText().replace("\n", "")

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

        return ApiResult.fromDeserializedModel(
            ApiManoEmployeeDetails(
                phones = phones,
                emails = emails,
                departments = departments,
                offices = offices,
                positions = positions,
                fullName = fullName ?: ""
            ), operation = op
        )
    }
}