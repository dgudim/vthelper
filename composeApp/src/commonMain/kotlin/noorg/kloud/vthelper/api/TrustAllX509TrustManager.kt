package noorg.kloud.vthelper.api

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.io.readByteArray
import java.security.cert.*
import javax.net.ssl.*

class TrustAllX509TrustManager : X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)

    override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {}

    override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
}