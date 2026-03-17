package noorg.kloud.vthelper.api.models

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

data class LoginResult(
    val statusCode: HttpStatusCode,
    val context: String,
    val responseContent: String,
    val isSuccessful: Boolean,
    val step: String
) {
    fun getFullStatus(): String {
        if (isSuccessful) {
            return "Completed successfully on step '$step'"
        }
        return "Failed at '$step' with status code '$statusCode'. Why: '$context'. Truncated content: '${
            responseContent.take(
                100
            )
        }...'"
    }

    companion object {
        suspend fun fromHttpResult(
            response: HttpResponse,
            context: String,
            isSuccessful: Boolean,
            step: String
        ): LoginResult {
            return LoginResult(
                statusCode = response.status,
                responseContent = response.bodyAsText(),
                context = context,
                isSuccessful = isSuccessful,
                step = step
            )
        }
    }
}

