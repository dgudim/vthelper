package noorg.kloud.vthelper.platform_specific

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

private const val APP_ID = "vthelper"

fun String.toPath(): Path = Path(this)

operator fun Path?.div(child: String): Path? = if (this != null) Path(this, child) else null

internal expect fun dataDirectory(appId: String): Path

internal expect fun cacheDirectory(appId: String): Path

fun appDataDirectory(createDir: Boolean = true): Path = dataDirectory(APP_ID).also {
    if (createDir) {
        SystemFileSystem.createDirectories(it)
    }
}

fun appCacheDirectory(createDir: Boolean = true): Path = cacheDirectory(APP_ID).also {
    if (createDir) {
        SystemFileSystem.createDirectories(it)
    }
}