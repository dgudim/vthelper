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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import noorg.kloud.vthelper.api.VTBaseApi.getPageWithSamlRefresh
import noorg.kloud.vthelper.api.VTBaseApi.refreshSamlForPageIfNeededUnsafe
import noorg.kloud.vthelper.api.models.NetResult
import noorg.kloud.vthelper.api.models.expect200
import noorg.kloud.vthelper.api.models.expectCode
import noorg.kloud.vthelper.api.models.moodle.ApiMoodleListCoursesRequestArgs
import noorg.kloud.vthelper.api.models.moodle.ApiMoodleListCoursesRequestRootElem
import noorg.kloud.vthelper.api.models.moodle.ApiMoodleListCoursesResponse
import noorg.kloud.vthelper.api.models.toNetResult
import noorg.kloud.vthelper.api.models.toNetResultFail
import noorg.kloud.vthelper.api.models.toNetResultOk
import noorg.kloud.vthelper.findFirstGroup
import noorg.kloud.vthelper.platform_specific.getHttpClientEngine
import kotlin.concurrent.Volatile

object MoodleApi {

    private val baseUrl = Url("https://moodle.vilniustech.lt")
    private val sessionKeyExtractionRegex = Regex("""sesskey=(.*?)"""", RegexOption.MULTILINE)
    private val userIdExtractionRegex = Regex("""data-userid="(.*?)"""", RegexOption.MULTILINE)
    private val calendarExportUrlExtractionRegex =
        Regex("""id="calendarexporturl".*value="(.*?)"""", RegexOption.MULTILINE)

    @Volatile
    private var sessionKey = ""

    @Volatile
    private var calendarUrl = ""

    @Volatile
    var userId: Int? = null
        private set

    val client = HttpClient(getHttpClientEngine()) {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(HttpCookies) {
            storage = VTBaseApi.cookieStorage
        }
        engine {
            dispatcher = Dispatchers.IO
        }
    }

    // TODO: Add caching to most endpoints

    suspend fun loginIfNeeded(
        studentId: String,
        password: String,
        mfaCode: String
    ): NetResult<String> {
        return VTBaseApi.loginIfNeeded(baseUrl, studentId, password, mfaCode, "login into moodle")
    }

    suspend fun updateSessionInfo(
    ): NetResult<String> {
        return safeRetry("update moodle session (user request)", 3) { op, previousResult ->
            updateSessionUnsafe(previousResult?.bodyRaw, op)
        }
    }

    // Don't allow refreshing the same session multiple times at the same time
    private val sessionRefreshMutex = Mutex()
    private suspend fun updateSessionUnsafe(
        prevCallBody: String?,
        rootOperationName: String
    ): NetResult<String> {
        sessionRefreshMutex.withLock {

            val pageContent =
                getPageWithSamlRefresh(rootOperationName, baseUrl, prevCallBody)
                    .onFailure { return this }
                    .bodyTyped ?: ""

            val extractedSessionKey =
                sessionKeyExtractionRegex.findFirstGroup(pageContent)
                    ?: return pageContent.toNetResultFail(
                        context = "Session key not found",
                        operation = "$rootOperationName + session key extraction"
                    )

            val extractedUserId =
                userIdExtractionRegex.findFirstGroup(pageContent)
                    ?: return pageContent.toNetResultFail(
                        context = "User ID not found",
                        operation = "$rootOperationName + user id extraction"
                    )

            sessionKey = extractedSessionKey
            userId = extractedUserId.toInt()

            return "Key: $extractedSessionKey; Uid: $extractedUserId".toNetResultOk(
                rootOperationName
            )
        }
    }

    suspend fun getCalendarUrl(
    ): NetResult<String> {
        return safeRetryWithPrecall(
            "get moodle calendar url", "update session",
            mainBlock = {
                getCalendarUrlUnsafe(it)
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionUnsafe(mainCallResult.bodyRaw, op)
            })
    }

    private suspend fun getCalendarUrlUnsafe(rootOperationName: String): NetResult<String> {

        require(sessionKey.isNotEmpty()) { "Session key is empty" }

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

        calendarUrlPageResponse.expect200<String>(
            operation = "$rootOperationName + main request"
        )?.let { return it }

        val calendarUrlPageContent = calendarUrlPageResponse.bodyAsText()
        val extractedCalendarUrl =
            calendarExportUrlExtractionRegex
                .findFirstGroup(calendarUrlPageContent)
                ?: return calendarUrlPageContent.toNetResultFail(
                    context = "Calendar URL not found",
                    operation = "$rootOperationName + calender url extraction"
                )

        calendarUrl = extractedCalendarUrl

        return extractedCalendarUrl.toNetResultOk(operation = rootOperationName)
    }

    suspend fun getCourses(): NetResult<ApiMoodleListCoursesResponse> {
        return safeRetryWithPrecall(
            "get moodle courses", "update session",
            mainBlock = {
                getCoursesUnsafe(it)
            },
            beforeRetryBlock = { op, mainCallResult ->
                updateSessionUnsafe(mainCallResult.bodyRaw, op)
            })
    }

    private suspend fun getCoursesUnsafe(rootOperationName: String): NetResult<ApiMoodleListCoursesResponse> {
        val methodName = "core_course_get_enrolled_courses_by_timeline_classification"

        require(sessionKey.isNotEmpty()) { "Session key is empty" }

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

        return coursesResponse.toNetResult<ApiMoodleListCoursesResponse>(
            isSuccess = true,
            successUpdater = { resp -> resp?.get(0)?.error == false },
            operation = rootOperationName
        )
    }
}