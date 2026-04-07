package noorg.kloud.vthelper.platform_specific

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory

private const val APP_ID = "vthelper"

fun String.toPath(): Path = Path(this)

// https://discuss.kotlinlang.org/t/keep-nullability-of-function-parameter-type-on-return-type/15233/4
operator fun <T: Path?> T.div(child: String): T = (if (this != null) Path(this, child) else null) as T

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

fun appTempDirectory(): Path = SystemTemporaryDirectory