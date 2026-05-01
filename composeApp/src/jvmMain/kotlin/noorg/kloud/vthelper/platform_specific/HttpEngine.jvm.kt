package noorg.kloud.vthelper.platform_specific

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpEngine
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

actual fun getHttpClientEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            val trustAllCerts = object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }

            val sslContext = SSLContext.getInstance("SSL").apply {
                init(null, arrayOf(trustAllCerts), SecureRandom())
            }

            sslSocketFactory(sslContext.socketFactory, trustAllCerts)

            // Important for ktor to handle all of this on its own, set by default
            // followRedirects(false)
            // followSslRedirects(false)
            // retryOnConnectionFailure(true)

            hostnameVerifier { _, _ -> true }
        }
    }
}