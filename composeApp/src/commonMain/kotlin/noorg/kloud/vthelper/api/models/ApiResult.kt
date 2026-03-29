package noorg.kloud.vthelper.api.models

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

data class ApiResult<T>(
    val statusCode: HttpStatusCode,
    val context: String,
    val bodyRaw: String?,
    val bodyTyped: T?,
    val isSuccessful: Boolean,
    val operation: String
) {
    fun getFullStatus(): String {
        if (isSuccessful) {
            return "Completed successfully on step '$operation'"
        }
        return "Failed at '$operation' with status code '$statusCode'. Why: '$context'. Truncated content: '${
            bodyRaw?.take(
                100
            )
        }...'"
    }

    fun logIt(): ApiResult<T> {
        println(getFullStatus())
        return this
    }

    companion object {
        // https://stackoverflow.com/questions/45949584/how-does-the-reified-keyword-in-kotlin-work
        suspend inline fun <reified T> fromHttpResult(
            response: HttpResponse,
            context: String? = null,
            isSuccessful: Boolean,
            operation: String
        ): ApiResult<T> {
            return ApiResult(
                statusCode = response.status,
                bodyRaw = if (!isSuccessful) response.bodyAsText() else null,
                bodyTyped = if (isSuccessful) response.body<T>() else null,
                context = context ?: (if (isSuccessful) "OK" else "FAIL"),
                isSuccessful = isSuccessful,
                operation = operation
            ).logIt()
        }

        fun <T> fromDeserializedModel(
            response: T,
            context: String? = null,
            operation: String
        ): ApiResult<T> {
            return ApiResult(
                statusCode = HttpStatusCode.OK,
                bodyRaw = null,
                bodyTyped = response,
                context = context ?: "OK",
                isSuccessful = true,
                operation = operation
            ).logIt()
        }

        suspend inline fun <reified T> expectCode(
            response: HttpResponse,
            context: String? = null,
            expectedCodes: List<HttpStatusCode>,
            operation: String
        ): ApiResult<T>? {
            if (response.status in expectedCodes) {
                return null
            }

            val expectedCodesMsg = "Unexpected return code, expected one of: $expectedCodes"

            return fromHttpResult<T>(
                response,
                if (context != null) "$context, $expectedCodesMsg" else expectedCodesMsg,
                false,
                operation
            )
        }

        suspend inline fun <reified T> expect200(
            response: HttpResponse,
            operation: String
        ): ApiResult<T>? {
            return expectCode<T>(
                response,
                null,
                listOf(HttpStatusCode.OK),
                operation
            )
        }
    }
}

suspend inline fun <reified T> HttpResponse.expect200(operation: String): ApiResult<T>? {
    return ApiResult.expect200(this, operation)
}

suspend inline fun <reified T> HttpResponse.toApiResult(
    context: String? = null,
    isSuccessful: Boolean,
    operation: String
): ApiResult<T> {
    return ApiResult.fromHttpResult(this, context, isSuccessful, operation)
}

suspend inline fun <reified T> HttpResponse.expectCode(
    context: String? = null,
    expectedCodes: List<HttpStatusCode>,
    operation: String
): ApiResult<T>? {
    return ApiResult.expectCode(this, context, expectedCodes, operation)
}

