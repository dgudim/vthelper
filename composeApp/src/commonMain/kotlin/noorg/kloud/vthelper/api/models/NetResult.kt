package noorg.kloud.vthelper.api.models

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import noorg.kloud.vthelper.fullMessage

data class NetResult<T>(
    val statusCode: HttpStatusCode,
    val context: String,
    val bodyRaw: String?,
    val bodyTyped: T?,
    val isSuccess: Boolean,
    val operation: String
) {

    val isFailure get() = !isSuccess

    fun getFullStatus(): String {
        if (isSuccess) {
            return "Completed successfully on step '$operation'. Why: '$context'"
        }
        return "Failed at '$operation' with status code '$statusCode'. Why: '$context'"
    }

    fun toResult(): Result<String> {
        return if (isSuccess) {
            Result.success(getFullStatus())
        } else {
            Result.failure(Exception(getFullStatus()))
        }
    }

    fun logIt(): NetResult<T> {
        println(getFullStatus())
        println(" == Truncated body: ${bodyRaw?.take(50)}\n")
        return this
    }

    companion object {
        // https://stackoverflow.com/questions/45949584/how-does-the-reified-keyword-in-kotlin-work
        suspend inline fun <reified T> fromHttpResult(
            response: HttpResponse,
            context: String? = null,
            isSuccess: Boolean,
            successUpdater: (T?) -> Boolean = { true },
            operation: String
        ): NetResult<T> {
            val bodyTyped = if (isSuccess) response.body<T>() else null
            return NetResult(
                statusCode = response.status,
                bodyRaw = if (!isSuccess) response.bodyAsText() else null,
                bodyTyped = bodyTyped,
                context = context ?: (if (isSuccess) "OK" else "FAIL"),
                isSuccess = isSuccess && successUpdater(bodyTyped),
                operation = operation
            ).logIt()
        }

        fun <T> fromDeserializedModelOk(
            response: T,
            operation: String
        ): NetResult<T> {
            return NetResult(
                statusCode = HttpStatusCode.OK,
                bodyRaw = null,
                bodyTyped = response,
                context = "OK",
                isSuccess = true,
                operation = operation
            ).logIt()
        }

        fun <T> fromBodyRawFail(
            bodyRaw: String,
            context: String,
            operation: String
        ): NetResult<T> {
            return NetResult<T>(
                statusCode = HttpStatusCode.ServiceUnavailable,
                bodyRaw = bodyRaw,
                bodyTyped = null,
                context = context,
                isSuccess = false,
                operation = operation
            ).logIt()
        }

        fun <T> fromException(
            exception: Throwable,
            operation: String
        ): NetResult<T> {
            return NetResult<T>(
                statusCode = HttpStatusCode.ServiceUnavailable,
                bodyRaw = exception.stackTraceToString(),
                bodyTyped = null,
                context = exception.fullMessage(),
                isSuccess = false,
                operation = operation
            ).logIt()
        }

        fun <T, R> fromOtherResult(otherResult: NetResult<R>): NetResult<T> {
            return NetResult<T>(
                statusCode = otherResult.statusCode,
                bodyRaw = otherResult.bodyRaw,
                bodyTyped = null,
                context = otherResult.context,
                isSuccess = otherResult.isSuccess,
                operation = otherResult.operation
            ).logIt()
        }

        suspend inline fun <reified T> expectCode(
            response: HttpResponse,
            context: String? = null,
            expectedCodes: List<HttpStatusCode>,
            operation: String
        ): NetResult<T>? {
            if (response.status in expectedCodes) {
                println("expectCode succeeded on '$operation', return code was '${response.status}'")
                return null
            }

            val expectedCodesMsg = "Unexpected return code, expected one of: $expectedCodes"

            return fromHttpResult<T>(
                response,
                context = if (context != null) "$context, $expectedCodesMsg" else expectedCodesMsg,
                isSuccess = false,
                operation = operation
            )
        }

        suspend inline fun <reified T> expect200(
            response: HttpResponse,
            operation: String
        ): NetResult<T>? {
            return expectCode<T>(
                response,
                null,
                listOf(HttpStatusCode.OK),
                operation
            )
        }
    }
}

suspend inline fun <reified T> HttpResponse.expect200(operation: String): NetResult<T>? {
    return NetResult.expect200(this, operation)
}

inline fun <reified T> String.toNetResultFail(context: String, operation: String): NetResult<T> {
    return NetResult.fromBodyRawFail(this, context, operation)
}

fun String.toNetResultOk(operation: String): NetResult<String> {
    return NetResult.fromDeserializedModelOk(this, operation)
}

suspend inline fun <reified T> HttpResponse.toNetResult(
    context: String? = null,
    isSuccess: Boolean,
    successUpdater: (T?) -> Boolean = { true },
    operation: String
): NetResult<T> {
    return NetResult.fromHttpResult(
        response = this,
        context = context,
        isSuccess = isSuccess,
        successUpdater = successUpdater,
        operation = operation
    )
}

suspend inline fun <reified T> HttpResponse.expectCode(
    context: String? = null,
    expectedCodes: List<HttpStatusCode>,
    operation: String
): NetResult<T>? {
    return NetResult.expectCode(
        response = this,
        context = context,
        expectedCodes = expectedCodes,
        operation = operation
    )
}

