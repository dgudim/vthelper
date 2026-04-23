package noorg.kloud.vthelper.api

import io.ktor.client.HttpClient
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import noorg.kloud.vthelper.api.models.NetResult
import noorg.kloud.vthelper.api.models.expect200
import noorg.kloud.vthelper.api.models.expectCode
import noorg.kloud.vthelper.api.models.toNetResult
import noorg.kloud.vthelper.api.models.toNetResultFail
import noorg.kloud.vthelper.api.models.toNetResultOk
import noorg.kloud.vthelper.findFirstGroup
import noorg.kloud.vthelper.platform_specific.getHttpClientEngine

object VTBaseApi {

    val cookieStorage = InMemoryCookieStorage()

    val authDomainBaseUrl = Url("https://fs.vilniustech.lt/")

    private val mfaExtractionRegex =
        Regex("""id="context".*?value="(.*?)"""", RegexOption.MULTILINE)
    val samlResponseRegex =
        Regex("""name="SAMLResponse" value="(.*?)"""", RegexOption.MULTILINE)
    val samlUrlRegex = Regex("""name="hiddenform" action="(.*?)"""", RegexOption.MULTILINE)

    val client = HttpClient(getHttpClientEngine()) {
        install(HttpCookies) {
            storage = cookieStorage
        }
        engine {
            dispatcher = Dispatchers.IO
        }
        followRedirects = false
    }

    val clientWithRedirects = HttpClient(getHttpClientEngine()) {
        install(HttpCookies) {
            storage = cookieStorage
        }
        engine {
            dispatcher = Dispatchers.IO
        }
        followRedirects = true
    }

    private val baseHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64; rv:149.0) Gecko/20100101 Firefox/149.0",
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
        "Origin" to authDomainBaseUrl.toString(),
        "Sec-Fetch-Site" to "same-origin",
    )

    private fun clearCookies() {
        cookieStorage.clear()
    }

    fun logout() {
        clearCookies()
    }

    suspend fun loginIfNeeded(
        serviceBaseUrl: Url,
        studentId: String,
        password: String,
        mfaCode: String,
        operation: String
    ): NetResult<String> {
        return safeRetry(operation, 3) { operation, _ ->
            loginIfNeededUnsafe(operation, serviceBaseUrl, studentId, password, mfaCode)
        }
    }

    private suspend fun loginIfNeededUnsafe(
        rootOperation: String,
        serviceBaseUrl: Url,
        studentId: String,
        password: String,
        mfaCode: String
    ): NetResult<String> {

        println("Logging into '$serviceBaseUrl'")

        var currentReferrer = authDomainBaseUrl.toString()

        // Try loading the page directly
        // MDL_SSP_SessID and MoodleSession are set if we are logging into moodle
        val initialResponse = client.get(serviceBaseUrl) {
            headers { appendAll(initialRequestHeaders + mapOf("Referer" to currentReferrer)) }
        }

        println("Initial request to '$serviceBaseUrl' done")

        if (initialResponse.status == HttpStatusCode.OK) { // 200
            return initialResponse.toNetResult(
                context = "Already logged in",
                isSuccess = true,
                operation = "$rootOperation + initial request"
            )
        }

        // 303 or 302. We should get a redirection if it's not 200
        initialResponse.expectCode<String>(
            expectedCodes = listOf(HttpStatusCode.SeeOther, HttpStatusCode.Found),
            operation = "$rootOperation + initial request"
        )?.let { return it }

        var redirectLocation = Url(initialResponse.headers[HttpHeaders.Location] ?: "")

        println("Redirecting to $redirectLocation")

        // Redirection to some other page on the same site, this is ok
        // We can also test by the status code, looks like it's 303 for normal redirects
        // and 302 for auth redirects, but I am not sure that it's always the case
        if (redirectLocation.host == serviceBaseUrl.host) {
            return initialResponse.toNetResult(
                context = "Already logged in, same-site redirection",
                isSuccess = true,
                operation = "$rootOperation + initial redirect"
            )
        }

        // Load the redirected page to see what we got
        // This doesn't normally set any cookies, unless it's a local session refresh
        val initialRedirectedResponse = client.get(redirectLocation) {
            headers { appendAll(initialRequestHeaders + mapOf("Referer" to currentReferrer)) }
        }

        initialRedirectedResponse.expect200<String>(
            "$rootOperation + load initial redirected page"
        )?.let { return it }

        val initialRedirectResponseContent = initialRedirectedResponse.bodyAsText()
        // Intermediate hidden form for SAML request, no relogin required, only local token/session refresh
        refreshSamlForPageIfNeededUnsafe(
            "$rootOperation + refresh saml (immediate)",
            serviceBaseUrl,
            initialRedirectResponseContent
        )?.let { return it }

        currentReferrer = redirectLocation.toString()

        println("Posting login details")

        // Otherwise it's a redirect to full login page
        // This sets MSISAuth cookie
        val loginResponse = client.submitForm(
            url = redirectLocation.toString(),
            formParameters = parameters {
                append("UserName", "university\\$studentId")
                append("Kmsi", "true") // 'Remember me' flag
                append("AuthMethod", "FormsAuthentication")
                append("Password", password)
            }
        ) {
            headers { appendAll(loginHeaders + mapOf("Referer" to currentReferrer)) }
        }

        // We must get 302, it we didn't, username or password is wrong
        loginResponse.expectCode<String>(
            context = "Wrong username or password",
            expectedCodes = listOf(HttpStatusCode.Found),
            operation = "$rootOperation + username login"
        )?.let { return it }

        // Same url, but different content, the page contains context key for MFA
        redirectLocation = Url(loginResponse.headers[HttpHeaders.Location] ?: "")

        println("Loading next mage (MFA)")

        // Load the redirected page to see what we got and extract mfa context
        // This doesn't set any cookies
        val initialMfaPageResponse = client.get(redirectLocation) {
            headers { appendAll(initialRequestHeaders + mapOf("Referer" to currentReferrer)) }
        }

        initialMfaPageResponse.expect200<String>(
            "$rootOperation + get MFA page after login"
        )?.let { return it }

        val initialMfaPageContent = initialMfaPageResponse.bodyAsText()
        var mfaContext =
            mfaExtractionRegex.findFirstGroup(initialMfaPageContent)
                ?: return initialMfaPageContent.toNetResultFail(
                    context = "MFA context is null",
                    operation = "$rootOperation + extract initial mfa context from page"
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
            headers { appendAll(loginHeaders + mapOf("Referer" to currentReferrer)) }
        }

        mfaContextPostResponse.expect200<String>(
            "$rootOperation + post initial MFA context"
        )?.let { return it }

        // Update mfa context
        val mfaContextPostContent = mfaContextPostResponse.bodyAsText()
        mfaContext =
            mfaExtractionRegex.findFirstGroup(mfaContextPostContent)
                ?: return mfaContextPostContent.toNetResultFail(
                    context = "Updated mfa context is null",
                    operation = "$rootOperation + extract updated MFA context from page"
                )


        println("Posting mfa code")

        // This resets MSISAuth on adfs/ls and sets the MSISAuth + MSISAuth1 on /adfs
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
            headers { appendAll(loginHeaders + mapOf("Referer" to currentReferrer)) }
        }

        // We must get 302, it we didn't, mfa code is wrong
        mfaCodeResponse.expectCode<String>(
            context = "Wrong mfa code",
            expectedCodes = listOf(HttpStatusCode.Found),
            operation = "$rootOperation + mfa login"
        )?.let {
            // NOTE: Currently the case when the use inputs an incorrect mfa code
            // and then inputs the correct one is not handled (we will get mfa form directly)
            // TODO: Handle this case
            logout() // Clear cookies to force full login flow on the next attempt
            return it
        }

        // Should be the same url
        currentReferrer = redirectLocation.toString()
        redirectLocation = Url(mfaCodeResponse.headers[HttpHeaders.Location] ?: "")

        println("Loading final redirect page (should be a saml hidden form)")

        // This sets a bunch of cookies, including SamlSession and MSISAuthenticated
        val finalSamlLoadRequest = client.get(redirectLocation) {
            headers { appendAll(initialRequestHeaders + mapOf("Referer" to currentReferrer)) }
        }

        // We must get a page with a hidden form for saml
        finalSamlLoadRequest.expect200<String>(
            "$rootOperation + load final saml hidden form"
        )?.let { return it }

        return refreshSamlUnsafe(
            "$rootOperation + refresh saml (the end)",
            serviceBaseUrl,
            finalSamlLoadRequest.bodyAsText(),
            currentReferrer
        )
    }

    suspend fun refreshSamlForPageIfNeededUnsafe(
        parentOperation: String,
        baseUrl: Url,
        bodyText: String?
    ): NetResult<String>? {
        val isSaml = bodyText != null && bodyText.contains("Working...")
        println("Checking if the page is a saml request: $isSaml")
        if (isSaml) {
            return refreshSamlUnsafe(
                parentOperation,
                baseUrl,
                bodyText,
                baseUrl.toString()
            )
        }
        return null
    }

    suspend fun getPageWithSamlRefresh(
        rootOperationName: String,
        baseUrl: Url,
        prevCallBody: String?
    ): NetResult<String> {
        refreshSamlForPageIfNeededUnsafe(
            "$rootOperationName + refresh saml",
            baseUrl,
            prevCallBody
        )?.onFailure { return this }

        val pageResponse = clientWithRedirects.get(baseUrl)

        pageResponse.expect200<String>(
            operation = "$rootOperationName + main request"
        )?.let { return it }

        var pageContent = pageResponse.bodyAsText()

        val secondaryRefreshResult = refreshSamlForPageIfNeededUnsafe(
            "$rootOperationName + refresh saml (new response)",
            baseUrl,
            pageContent
        )?.onFailure { return this }

        // If the saml session was refreshed, get the body after the refresh
        if (secondaryRefreshResult?.isSuccess == true) {
            pageContent = secondaryRefreshResult.bodyTyped ?: ""
        }

        return pageContent.toNetResultOk(rootOperationName)
    }

    // This extracts and posts saml response to the corresponding mano or moodle endpoint and refreshes session cookies
    suspend fun refreshSamlUnsafe(
        parentOperation: String,
        serviceBaseUrl: Url,
        pageContent: String,
        currentReferrer: String
    ): NetResult<String> {
        val samlResponse = samlResponseRegex.findFirstGroup(pageContent)
        val samlUrl = samlUrlRegex.findFirstGroup(pageContent)
            ?.replace(":443/", "/") // Remove explicit https port

        if (samlResponse == null || samlUrl == null) {
            return NetResult<String>(
                statusCode = HttpStatusCode.OK,
                bodyRaw = pageContent,
                bodyTyped = null,
                context = "Saml response or url is null",
                isSuccess = false,
                operation = "$parentOperation + extract saml response"
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
            headers { appendAll(loginHeaders + mapOf("Referer" to currentReferrer)) }
        }

        // We must get a redirect to the actual service page
        serviceSamlAuthResponse.expectCode<String>(
            expectedCodes = listOf(HttpStatusCode.SeeOther),
            operation = "$parentOperation + post sam auth data"
        )?.let { return it }

        val redirectedServiceBaseUrl = serviceSamlAuthResponse.headers[HttpHeaders.Location] ?: ""

        // Should be identical
        println("Final request to $redirectedServiceBaseUrl (Ours: '$serviceBaseUrl')")

        // This sets session cookies specific to the service
        // we may get a redirection to some other page on the same site (e.g: moodle's /my)
        // that's why we use the redirect client here
        val finalServiceResponse = clientWithRedirects.get(redirectedServiceBaseUrl) {
            headers { appendAll(initialRequestHeaders + mapOf("Referer" to samlUrl)) }
        }

        finalServiceResponse.expect200<String>(
            operation = "$parentOperation + final service request"
        )?.let { return it }

        return finalServiceResponse.toNetResult(
            isSuccess = true,
            operation = parentOperation
        )
    }
}