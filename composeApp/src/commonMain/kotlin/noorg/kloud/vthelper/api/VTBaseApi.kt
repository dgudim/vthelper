package noorg.kloud.vthelper.api

import io.ktor.client.HttpClient

class VTBaseApi {

    private val client = HttpClient()

    suspend fun loginIfNeeded(username: String, password: String) {

    }

}