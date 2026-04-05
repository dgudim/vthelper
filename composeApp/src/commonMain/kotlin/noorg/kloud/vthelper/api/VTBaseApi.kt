package noorg.kloud.vthelper.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.parameters
import io.ktor.util.appendAll
import noorg.kloud.vthelper.api.models.ApiResult
import noorg.kloud.vthelper.api.models.expect200
import noorg.kloud.vthelper.api.models.expectCode
import noorg.kloud.vthelper.api.models.toApiResult
import noorg.kloud.vthelper.findFirstGroup

object VTBaseApi {

    var cookieStorage = AcceptAllCookiesStorage()

    private val mfaExtractionRegex =
        Regex("""id="context".*?value="(.*?)"""", RegexOption.MULTILINE)
    val samlResponseRegex =
        Regex("""name="SAMLResponse" value="(.*?)"""", RegexOption.MULTILINE)
    val samlUrlRegex = Regex("""name="hiddenform" action="(.*?)"""", RegexOption.MULTILINE)

    private val client = HttpClient(getHttpClientEngine()) {
        install(HttpCookies) {
            storage = cookieStorage
        }
        followRedirects = false
    }

    private val baseHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64; rv:148.0) Gecko/20100101 Firefox/148.0",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language" to "en-US,en;q=0.9",
        "DNT" to "1",
        "Sec-GPC" to "1",
        "Connection" to "keep-alive",
        "Upgrade-Insecure-Requests" to "1",
        "Sec-Fetch-Dest" to "document",
        "Sec-Fetch-Mode" to "navigate",
        "Sec-Fetch-User" to "?1",
        "Priority" to "u=0, i",
        "Pragma" to "no-cache",
        "Cache-Control" to "no-cache"
    )

    // Headers used for the initial page request
    private val initialRequestHeaders = baseHeaders + mapOf(
        "Sec-Fetch-Site" to "none"
    )

    // Headers used for all the login and auth requests
    private val loginHeaders = baseHeaders + mapOf(
        "Content-Type" to "application/x-www-form-urlencoded",
        "Origin" to "https://fs.vilniustech.lt",
        "Sec-Fetch-Site" to "same-origin",
    )

    private suspend fun loadCookies() {

    }

    private suspend fun persistCookies() {

    }

    private fun clearCookies() {
        cookieStorage = AcceptAllCookiesStorage()
    }

    suspend fun loginIfNeeded(
        serviceBaseUrl: Url,
        username: String,
        password: String,
        mfaCode: String
    ): ApiResult<String> {

        println("Random bullsh*t, go! '$serviceBaseUrl'")

        // Try loading the page directly
        // MDL_SSP_SessID and MoodleSession are set if we are logging into moodle
        val initialResponse = client.get(serviceBaseUrl) {
            headers { appendAll(initialRequestHeaders) }
        }

        println("Initial request to '$serviceBaseUrl' done")

        if (initialResponse.status == HttpStatusCode.OK) { // 200
            return initialResponse.toApiResult(
                context = "Already logged in",
                isSuccessful = true,
                operation = "initial request"
            )
        }

        // 303 or 302. We should get a redirection if it's not 200
        initialResponse.expectCode<String>(
            expectedCodes = listOf(HttpStatusCode.SeeOther, HttpStatusCode.Found),
            operation = "initial request"
        )?.let { return it }

        var redirectLocation = Url(initialResponse.headers[HttpHeaders.Location] ?: "")

        println("Redirecting to $redirectLocation")

        // Redirection to some other page on the same site, this is ok
        // We can also test by the status code, looks like it's 303 for normal redirects
        // and 302 for auth redirects, but I am not sure that it's always the case
        if (redirectLocation.host == serviceBaseUrl.host) {
            return initialResponse.toApiResult(
                context = "Already logged in, same-site redirection",
                isSuccessful = true,
                operation = "initial request"
            )

        }

        // Load the redirected page to see what we got
        // This doesn't normally set any cookies, unless it's a local session sefresh
        val initialRedirectedResponse = client.get(redirectLocation) {
            headers { appendAll(initialRequestHeaders) }
        }

        initialRedirectedResponse.expect200<String>(
            "load initial redirected page"
        )?.let { return it }

        val initialRedirectResponseContent = initialRedirectedResponse.bodyAsText()
        // Intermediate hidden form for SAML request, not relogin required, only local token/session refresh
        if (initialRedirectResponseContent.contains("Working...")) {
            return refreshLocalLogin(serviceBaseUrl, initialRedirectResponseContent)
        }

        println("Posting login details")

        // Otherwise it's a redirect to full login page
        // This sets MSISAuth cookie
        val loginResponse = client.submitForm(
            url = redirectLocation.toString(),
            formParameters = parameters {
                append("UserName", "university\\$username")
                append("Kmsi", "true") // 'Remember me' flag
                append("AuthMethod", "FormsAuthentication")
                append("Password", password)
            }
        ) {
            headers { appendAll(loginHeaders + mapOf("Referer" to redirectLocation.toString())) }
        }

        // We must get 302, it we didn't, username or password is wrong
        loginResponse.expectCode<String>(
            context = "Wrong username or password",
            expectedCodes = listOf(HttpStatusCode.Found),
            operation = "username login"
        )?.let { return it }

        // Same url, but different content, the page contains context key for MFA
        redirectLocation = Url(loginResponse.headers[HttpHeaders.Location] ?: "")

        println("Loading next mage (mfa)")

        // Load the redirected page to see what we got and extract mfa context
        // This doesn't set any cookies
        val initialMfaPageResponse = client.get(redirectLocation) {
            headers { appendAll(initialRequestHeaders) }
        }

        initialMfaPageResponse.expect200<String>(
            "get mfa page after login"
        )?.let { return it }

        var mfaContext =
            mfaExtractionRegex.findFirstGroup(initialMfaPageResponse.bodyAsText())
                ?: return initialMfaPageResponse.toApiResult(
                    context = "Mfa context is null",
                    isSuccessful = false,
                    operation = "extract initial mfa context from page"
                )


        println("Posting initial mfa request")

        // This doesn't set any cookies
        val mfaContextPostResponse = client.submitForm(
            url = redirectLocation.toString(),
            formParameters = parameters {
                append("AuthMethod", "AzureMfaAuthentication")
                append("Context", mfaContext)
                append("__EVENTTARGET", "")
            }
        ) {
            headers { appendAll(loginHeaders + mapOf("Referer" to redirectLocation.toString())) }
        }

        mfaContextPostResponse.expect200<String>(
            "post initial mfa context"
        )?.let { return it }

        mfaContext =
            mfaExtractionRegex.findFirstGroup(mfaContextPostResponse.bodyAsText())
                ?: return mfaContextPostResponse.toApiResult(
                    context = "Mfa context is null",
                    isSuccessful = false,
                    operation = "extract updated mfa context from page"
                )


        println("Posting mfa code")

        // This resets MSISAuth cookie and sets the MSISAuth1 cookie
        val mfaCodeResponse = client.submitForm(
            url = redirectLocation.toString(),
            formParameters = parameters {
                append("AuthMethod", "AzureMfaAuthentication")
                append("Context", mfaContext)
                append("__EVENTTARGET", "")
                append("VerificationCode", mfaCode)
                append("SignIn", "Sign in")
            }
        ) {
            headers { appendAll(loginHeaders + mapOf("Referer" to redirectLocation.toString())) }
        }

        // We must get 302, it we didn't, mfa code is wrong
        mfaCodeResponse.expectCode<String>(
            context = "Wrong mfa code",
            expectedCodes = listOf(HttpStatusCode.Found),
            operation = "mfa login"
        )?.let { return it }

        // Same url
        redirectLocation = Url(mfaCodeResponse.headers[HttpHeaders.Location] ?: "")

        println("Loading final redirect page (should be a saml hidden form)")

        // This sets a bunch of cookies, including SamlSession and MSISAuthenticated and clears MSISAuth1 cookie
        val finalSamlLoadRequest = client.get(redirectLocation) {
            headers { appendAll(initialRequestHeaders) }
        }

        // We must get a page with a hidden form for saml
        finalSamlLoadRequest.expect200<String>(
            "load final saml hidden form"
        )?.let { return it }

        return refreshLocalLogin(serviceBaseUrl, finalSamlLoadRequest.bodyAsText())
    }

    // This extracts and posts saml response to the corresponding mano or moodle endpoint and refreshes session cookies
    suspend fun refreshLocalLogin(serviceBaseUrl: Url, pageContent: String): ApiResult<String> {
        val samlResponse = samlResponseRegex.findFirstGroup(pageContent)
        val samlUrl = samlUrlRegex.findFirstGroup(pageContent)
            ?.replace(":443/", "/") // Remove explicit https port

        if (samlResponse == null || samlUrl == null) {
            return ApiResult<String>(
                statusCode = HttpStatusCode.OK,
                bodyRaw = pageContent,
                bodyTyped = null,
                context = "Saml response or url is null",
                isSuccessful = false,
                operation = "extract saml response"
            ).logIt()
        }

        println("Refreshing local login, posting to '$samlUrl'")

        // This sets cookies specific to the saml session (SimpleSAMLAuthToken and SimpleSAMLSessionID for mano for example)
        val serviceSamlAuthResponse = client.submitForm(
            url = samlUrl,
            formParameters = parameters {
                append("SAMLResponse", samlResponse)
                append("RelayState", serviceBaseUrl.toString())
            }
        ) {
            headers { appendAll(loginHeaders + mapOf("Referer" to (loginHeaders["Origin"] ?: ""))) }
        }

        // We must get a redirect to the actual service page
        serviceSamlAuthResponse.expectCode<String>(
            context = "Unexpected response code (expected 303)",
            expectedCodes = listOf(HttpStatusCode.SeeOther),
            operation = "post sam auth data"
        )?.let { return it }

        val redirectedServiceBaseUrl = serviceSamlAuthResponse.headers[HttpHeaders.Location] ?: ""

        // Should be identical
        println("Final request to $redirectedServiceBaseUrl (Ours: '$serviceBaseUrl')")

        // This sets session cookies specific to the service
        val finalServiceResponse = client.get(redirectedServiceBaseUrl) {
            headers { appendAll(initialRequestHeaders) }
        }

        // 303 is ok here as we may get a redirection to some other page on the same site (e.g: moodle's /my)
        finalServiceResponse.expectCode<String>(
            expectedCodes = listOf(HttpStatusCode.OK, HttpStatusCode.SeeOther),
            operation = "final service request"
        )?.let { return it }

        println("We are golden!")

        return finalServiceResponse.toApiResult(
            context = "Logged in successfully",
            isSuccessful = true,
            operation = "final service request"
        )
    }
}