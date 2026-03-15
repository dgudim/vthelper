import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.io.File

data class LoginResult(
    val rawStatusCode: Int,
    val rawContent: String,
    val isError: Boolean
)

class MoodleAuthenticator(
    private val userId: String = "",
    private val userPass: String = ""
) {
    private val moodleBaseUrl = "https://moodle.vilniustech.lt"
    private val moodleSamlEndpoint = "$moodleBaseUrl/auth/saml2/sp/saml2-acs.php/moodle.vilniustech.lt"

    // Setup client with a persistent cookie storage
    private val client = HttpClient(CIO) {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage() // In production, use a persistent storage
        }
        // We handle redirects manually to match the Python logic
        followRedirects = false
    }

    private val baseHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64; rv:148.0) Gecko/20100101 Firefox/148.0",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "DNT" to "1",
        "Upgrade-Insecure-Requests" to "1"
    )

    suspend fun loginIfNeeded(): LoginResult {
        println("Sent initial load request")
        val initialResponse = client.get(moodleBaseUrl) {
            baseHeaders.forEach { (k, v) -> header(k, v) }
        }

        if (initialResponse.status == HttpStatusCode.OK) {
            return LoginResult(200, initialResponse.bodyAsText(), false)
        }

        if (initialResponse.status != HttpStatusCode.SeeOther) { // 303
            return LoginResult(initialResponse.status.value, initialResponse.bodyAsText(), true)
        }

        var redirLocation = initialResponse.headers[HttpHeaders.Location] ?: ""
        println("Redirecting to $redirLocation")

        if (redirLocation.trimEnd('/') == "$moodleBaseUrl/my") {
            return LoginResult(303, initialResponse.bodyAsText(), false)
        }

        // Dummy GET to auth provider
        val dummyResponse = client.get(redirLocation) {
            baseHeaders.forEach { (k, v) -> header(k, v) }
        }

        if (dummyResponse.status != HttpStatusCode.OK) return errorResult(dummyResponse)

        // Post Login Details
        println("Posting login details")
        val loginResponse = client.submitForm(
            url = redirLocation,
            formParameters = parameters {
                append("UserName", "university\\$userId")
                append("Kmsi", "true")
                append("AuthMethod", "FormsAuthentication")
                append("Password", userPass)
            }
        ) {
            header(HttpHeaders.Origin, "https://fs.vilniustech.lt")
            header(HttpHeaders.Referer, redirLocation)
        }

        if (loginResponse.status != HttpStatusCode.Found) { // 302
            return errorResult(loginResponse)
        }

        redirLocation = loginResponse.headers[HttpHeaders.Location] ?: ""

        // MFA Context Extraction
        val mfaPageResponse = client.get(redirLocation)
        val context = extractContext(mfaPageResponse.bodyAsText()) ?: return errorResult(mfaPageResponse)

        // Initial MFA Request
        println("Requesting MFA code...")
        val mfaInitResponse = client.submitForm(
            url = redirLocation,
            formParameters = parameters {
                append("AuthMethod", "AzureMfaAuthentication")
                append("Context", context)
                append("__EVENTTARGET", "")
            }
        )

        val updatedContext = extractContext(mfaInitResponse.bodyAsText()) ?: return errorResult(mfaInitResponse)

        print("Please input 2fa code: ")
        val code = readLine()?.trim() ?: ""

        // Submit MFA Code
        val mfaCodeResponse = client.submitForm(
            url = redirLocation,
            formParameters = parameters {
                append("AuthMethod", "AzureMfaAuthentication")
                append("Context", updatedContext)
                append("__EVENTTARGET", "")
                append("VerificationCode", code)
                append("SignIn", "Sign in")
            }
        )

        if (mfaCodeResponse.status != HttpStatusCode.Found) return errorResult(mfaCodeResponse)

        // Final SAML Exchange
        val samlPageResponse = client.get(redirLocation)
        val samlResponse = extractSamlResponse(samlPageResponse.bodyAsText()) ?: return errorResult(samlPageResponse)

        println("Posting SAML to Moodle")
        val moodleAuthResponse = client.submitForm(
            url = moodleSamlEndpoint,
            formParameters = parameters {
                append("SAMLResponse", samlResponse)
                append("RelayState", "$moodleBaseUrl/")
            }
        ) {
            header(HttpHeaders.Referer, "https://fs.vilniustech.lt")
        }

        if (moodleAuthResponse.status != HttpStatusCode.SeeOther) return errorResult(moodleAuthResponse)

        val finalUrl = moodleAuthResponse.headers[HttpHeaders.Location] ?: ""
        val finalResponse = client.get(finalUrl)

        println("SUCCESS!!!")
        return LoginResult(finalResponse.status.value, finalResponse.bodyAsText(), false)
    }

    private fun extractSamlResponse(html: String): String? {
        val regex = Regex("""name="SAMLResponse" value="(.*?)"""", RegexOption.DOT_MATCHES_ALL)
        return regex.find(html)?.groupValues?.get(1)
    }

    private fun extractContext(html: String): String? {
        val regex = Regex("""id="context".*?value="(.*?)"""", RegexOption.DOT_MATCHES_ALL)
        return regex.find(html)?.groupValues?.get(1)
    }

    private suspend fun errorResult(response: HttpResponse) =
        LoginResult(response.status.value, response.bodyAsText(), true)
}

fun main() = runBlocking {
    val auth = MoodleAuthenticator("your_user", "your_pass")
    auth.loginIfNeeded()
}
