package noorg.kloud.vthelper.platform_specific

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

// https://medium.com/@ghasemdev/ktor-client-pipelines-deep-dive-e22d64522566

expect fun getHttpClientEngine(): HttpClientEngine

class EngineFactory : HttpClientEngineFactory<HttpClientEngineConfig> {
    override fun create(block: HttpClientEngineConfig.() -> Unit): HttpClientEngine {
        val engine = getHttpClientEngine()
        engine.config.apply { block() }
        return engine
    }
}

private val factory = EngineFactory()

fun getHttpClientBase(): HttpClient {
    return HttpClient(factory) {
        engine {
            dispatcher = Dispatchers.IO
            // https://en.wikipedia.org/wiki/HTTP_pipelining
            pipelining = true
        }
        followRedirects = true
    }
}