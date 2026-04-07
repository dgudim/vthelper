package noorg.kloud.vthelper.api

import io.ktor.client.HttpClient
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
import noorg.kloud.vthelper.api.models.NetResult
import noorg.kloud.vthelper.api.models.expect200
import noorg.kloud.vthelper.api.models.expectCode
import noorg.kloud.vthelper.api.models.moodle.ApiMoodleListCoursesRequestArgs
import noorg.kloud.vthelper.api.models.moodle.ApiMoodleListCoursesRequestRootElem
import noorg.kloud.vthelper.api.models.moodle.ApiMoodleListCoursesResponse
import noorg.kloud.vthelper.api.models.toApiResult
import noorg.kloud.vthelper.findFirstGroup
import noorg.kloud.vthelper.platform_specific.getHttpClientEngine

object MoodleApi {

    val baseUrl = Url("https://moodle.vilniustech.lt/")
    val sessionKeyExtractionRegex = Regex("""sesskey=(.*?)"""", RegexOption.MULTILINE)
    val userIdExtractionRegex = Regex("""data-userid="(.*?)"""", RegexOption.MULTILINE)
    val calendarExportUrlExtractionRegex =
        Regex("""id="calendarexporturl".*value="(.*?)"""", RegexOption.MULTILINE)

    private var sessionKey = ""
    private var calendarUrl = ""
    var userId: Int? = null
        private set

    val client = HttpClient(getHttpClientEngine()) {
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
        studentId: String,
        password: String,
        mfaCode: String
    ): NetResult<String> {
        return VTBaseApi.loginIfNeeded(baseUrl, studentId, password, mfaCode, "login into moodle")
    }

    suspend fun updateSessionInfo(
    ): NetResult<String> {
        return safeNetCall("update moodle session (user request)") {
            updateSessionInfoUnsafe(it)
        }
    }

    private suspend fun updateSessionInfoUnsafe(rootOperationName: String): NetResult<String> {
        val pageResponse = client.get(baseUrl)

        pageResponse.expect200<String>(
            operation = "$rootOperationName + main request"
        )?.let { return it }

        val extractedSessionKey =
            sessionKeyExtractionRegex.findFirstGroup(pageResponse.bodyAsText())
                ?: return pageResponse.toApiResult(
                    context = "Session key not found",
                    isSuccess = false,
                    operation = "$rootOperationName + session key extraction"
                )

        val extractedUserId =
            userIdExtractionRegex.findFirstGroup(pageResponse.bodyAsText())
                ?: return pageResponse.toApiResult(
                    context = "User ID not found",
                    isSuccess = false,
                    operation = "$rootOperationName + user id extraction"
                )

        sessionKey = extractedSessionKey
        userId = extractedUserId.toInt()

        return pageResponse.toApiResult(
            isSuccess = true,
            operation = rootOperationName
        )
    }

    suspend fun getCalendarUrl(
    ): NetResult<String> {
        return safeRetryOnDirectApiError(
            "get moodle calendar url", "update session",
            mainBlock = {
                getCalendarUrlUnsafe(it)
            },
            beforeRetryBlock = {
                updateSessionInfoUnsafe(it)
            })
    }

    suspend fun getCalendarUrlUnsafe(rootOperationName: String): NetResult<String> {
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

        calendarUrlPageResponse.expectCode<String>(
            expectedCodes = listOf(HttpStatusCode.OK),
            operation = "$rootOperationName + main request"
        )?.let { return it }

        val extractedCalendarUrl =
            calendarExportUrlExtractionRegex
                .findFirstGroup(calendarUrlPageResponse.bodyAsText())
                ?: return calendarUrlPageResponse.toApiResult(
                    context = "Calendar URL not found",
                    isSuccess = false,
                    operation = "$rootOperationName + calender url extraction"
                )

        calendarUrl = extractedCalendarUrl

        return calendarUrlPageResponse.toApiResult(
            isSuccess = true,
            operation = rootOperationName
        )
    }

    suspend fun getCourses(): NetResult<ApiMoodleListCoursesResponse> {
        return safeRetryOnDirectApiError(
            "get moodle calendar url", "update session",
            mainBlock = {
                getCoursesUnsafe(it)
            },
            beforeRetryBlock = {
                updateSessionInfoUnsafe(it)
            })
    }

    suspend fun getCoursesUnsafe(rootOperationName: String): NetResult<ApiMoodleListCoursesResponse> {
        val methodName = "core_course_get_enrolled_courses_by_timeline_classification"

        if (sessionKey.isEmpty()) {
            updateSessionInfoUnsafe("$rootOperationName + get session key").let {
                if (!it.isSuccess) {
                    return NetResult.fromOtherResult(it)
                }
            }
        }

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

        coursesResponse.expect200<ApiMoodleListCoursesResponse>(
            operation = "$rootOperationName + main request"
        )?.let { return it }

        return coursesResponse.toApiResult<ApiMoodleListCoursesResponse>(
            isSuccess = true,
            successUpdater = { resp -> resp?.get(0)?.error == false },
            operation = rootOperationName
        )
    }
}