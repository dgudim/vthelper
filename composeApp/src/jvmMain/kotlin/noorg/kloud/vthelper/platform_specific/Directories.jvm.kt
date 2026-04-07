package noorg.kloud.vthelper.platform_specific

import kotlinx.io.files.Path

fun desktopAppHomeDirectory(appId: String, os: OS, getEnv: (String) -> String?): Path {
    val osSpecificPath = when (os) {
        is OS.MacOs -> getEnv("HOME")?.toPath() / "Library/Application Support" / appId
        is OS.Windows -> getEnv("APPDATA")?.toPath() / appId
        is OS.Linux -> getEnv("XDG_DATA_HOME")?.toPath() / appId
        else -> null
    }
    osSpecificPath?.let { return it }
    val genericHomePath = getEnv("HOME")?.toPath() / ".$appId"
    genericHomePath?.let { return it }
    return ".$appId".toPath()
}


fun desktopCacheDirectory(appId: String, os: OS, getEnv: (String) -> String?): Path {
    val osSpecificPath = when (os) {
        is OS.MacOs -> getEnv("HOME")?.toPath() / "Library/Caches" / appId
        is OS.Windows -> getEnv("APPDATA")?.toPath() / "Caches" / appId
        is OS.Linux -> getEnv("XDG_CACHE_HOME")?.toPath() / appId
        else -> null
    }
    osSpecificPath?.let { return it }
    val genericHomePath = getEnv("HOME")?.toPath() / ".cache" / appId
    genericHomePath?.let { return it }
    return ".cache/.$appId".toPath()
}

private fun getEnvNullIfEmpty(variable: String): String? {
    return System.getenv(variable).ifEmpty { null }
}

actual fun dataDirectory(appId: String) = desktopAppHomeDirectory(
    appId = appId,
    os = hostOs,
    getEnv = ::getEnvNullIfEmpty,
)

actual fun cacheDirectory(appId: String) = desktopCacheDirectory(
    appId = appId,
    os = hostOs,
    getEnv = ::getEnvNullIfEmpty,
)