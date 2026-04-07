package noorg.kloud.vthelper

import kotlinx.io.files.Path
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal actual fun dataDirectory(appId: String): Path =
    NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
        .firstOrNull()?.toString()?.toPath()
        ?.let { it / appId } ?: error("Unable to get 'NSApplicationSupportDirectory'")

internal actual fun cacheDirectory(appId: String): Path {
    val cachesDirectory =
        NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
            .firstOrNull()?.toString()?.toPath() ?: error("Unable to get 'NSCachesDirectory'")

    return if (platform() is OS.MacOs) {
        (cachesDirectory / appId)!!
    } else {
        cachesDirectory
    }
}