package noorg.kloud.vthelper.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import noorg.kloud.vthelper.api.models.ApiResult
import noorg.kloud.vthelper.api.models.expect200
import noorg.kloud.vthelper.api.models.expectCode
import noorg.kloud.vthelper.api.models.moodle.ApiMoodleListCoursesRequestArgs
import noorg.kloud.vthelper.api.models.moodle.ApiMoodleListCoursesRequestRootElem
import noorg.kloud.vthelper.api.models.moodle.ApiMoodleListCoursesResponse
import noorg.kloud.vthelper.api.models.toApiResult
import noorg.kloud.vthelper.findFirstGroup

class MoodleApi {
    companion object {
        val baseUrl = Url("https://moodle.vilniustech.lt/")
        val sessionKeyExtractionRegex = Regex("""sesskey=(.*?)"""", RegexOption.MULTILINE)
        val userIdExtractionRegex = Regex("""data-userid="(.*?)"""", RegexOption.MULTILINE)
        val calendarExportUrlExtractionRegex =
            Regex("""id="calendarexporturl".*value="(.*?)"""", RegexOption.MULTILINE)
    }

    private var sessionKey = ""
    private var calendarUrl = ""
    private var userId = 0

    private suspend fun loadData() {

    }

    private suspend fun persistData() {

    }

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
            })
        }
        install(HttpCookies) {
            storage = VTBaseApi.cookieStorage
        }
    }

    suspend fun loginIfNeeded(
        username: String,
        password: String,
        mfaCode: String
    ) {
        VTBaseApi.loginIfNeeded(baseUrl, username, password, mfaCode)
    }

    suspend fun extractSessonInfo(): ApiResult<String> {
        val pageResponse = client.get(baseUrl)

        val operation = "session key extraction"

        pageResponse.expect200<String>(
            operation = operation
        )?.let { return it }

        val extractedSessionKey =
            sessionKeyExtractionRegex.findFirstGroup(pageResponse.bodyAsText())
                ?: return pageResponse.toApiResult(
                    "Session key not found",
                    false,
                    operation
                )

        val extractedUserId =
            userIdExtractionRegex.findFirstGroup(pageResponse.bodyAsText())
                ?: return pageResponse.toApiResult(
                    "User ID not found",
                    false,
                    operation
                )

        sessionKey = extractedSessionKey
        userId = extractedUserId.toInt()

        return pageResponse.toApiResult(
            context = "Extracted session info",
            isSuccessful = true,
            operation = operation
        )
    }

    suspend fun getCalendarUrl(): ApiResult<String> {
        val calendarUrlPageResponse = client.submitForm(
            url = "$baseUrl/calendar/export.php",
            formParameters = parameters {
                append("sesskey", sessionKey)
                append("_qf__core_calendar_export_form", "1")
                append("events[exportevents]", "all")
                append("period[timeperiod]", "recentupcoming")
                append("generateurl", "Get calendar URL")
            }
        )

        val operation = "calendar url extraction"

        calendarUrlPageResponse.expectCode<String>(
            expectedCodes = listOf(HttpStatusCode.OK),
            operation = operation
        )?.let { return it }

        val extractedCalendarUrl =
            calendarExportUrlExtractionRegex
                .findFirstGroup(calendarUrlPageResponse.bodyAsText())
                ?: return calendarUrlPageResponse.toApiResult(
                    "Calendar URL not found",
                    false,
                    operation
                )

        calendarUrl = extractedCalendarUrl

        return calendarUrlPageResponse.toApiResult(
            context = "Got calendar url",
            isSuccessful = true,
            operation = operation
        )
    }

    suspend fun getCourses(): ApiResult<ApiMoodleListCoursesResponse> {
        val methodName = "core_course_get_enrolled_courses_by_timeline_classification"

        val coursesResponse =
            client.post("$baseUrl/lib/ajax/service.php?sesskey=$sessionKey&info=$methodName") {
                contentType(ContentType.Application.Json)
                setBody(
                    listOf(
                        ApiMoodleListCoursesRequestRootElem(
                            index = 0,
                            methodName = methodName,
                            args = ApiMoodleListCoursesRequestArgs(
                                classification = "all",
                                customFieldName = "",
                                customFieldValue = "",
                                limit = 0,
                                offset = 0,
                                sort = "ul.timeaccess desc",
                                requiredFields = listOf(
                                    "id",
                                    "fullname",
                                    "shortname",
                                    "showcoursecategory",
                                    "showshortname",
                                    "visible",
                                    "enddate"
                                )
                            )
                        )
                    )
                )
            }

        val operation = "get courses"

        coursesResponse.expect200<ApiMoodleListCoursesResponse>(
            operation = operation
        )?.let { return it }

        return coursesResponse.toApiResult(
            context = "Got courses",
            isSuccessful = true,
            operation = operation
        )
    }
}