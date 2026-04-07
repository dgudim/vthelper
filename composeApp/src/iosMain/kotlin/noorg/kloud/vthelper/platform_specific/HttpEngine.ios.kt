package noorg.kloud.vthelper.platform_specific

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Foundation.credentialForTrust
import platform.Foundation.serverTrust

@OptIn(ExperimentalForeignApi::class)
actual fun getHttpClientEngine(): HttpClientEngine {
    return Darwin.create {
        // TODO: Handle later
//        handleChallenge { session, task, challenge, completionHandler ->
//            if (challenge.protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust) {
//                val cred = NSURLCredential.credentialForTrust(challenge.protectionSpace.serverTrust)
//                completionHandler(NSURLSessionAuthChallengeUseCredential, cred)
//            } else {
//                completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
//            }
//        }
    }
}