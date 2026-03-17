package noorg.kloud.vthelper.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class MoodleApi {
    companion object {
        val baseUrl = Url("https://moodle.vilniustech.lt/")
    }

    private var sessionKey = ""

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
            })
        }
    }

    suspend fun loginIfNeeded(
        username: String,
        password: String,
        mfaCode: String
    ) {
        VTBaseApi.loginIfNeeded(baseUrl, username, password, mfaCode)
    }

    suspend fun extractSessonKey() {

    }
}