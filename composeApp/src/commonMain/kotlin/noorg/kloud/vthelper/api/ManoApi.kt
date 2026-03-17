package noorg.kloud.vthelper.api

import io.ktor.http.Url

class ManoApi {
    companion object {
        val baseUrl = Url("https://mano.vilniustech.lt/")
    }

    suspend fun loginIfNeeded(username: String,
                      password: String,
                      mfaCode: String) {
        VTBaseApi.loginIfNeeded(baseUrl, username, password, mfaCode)
    }

}