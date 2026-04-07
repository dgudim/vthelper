package noorg.kloud.vthelper.platform_specific

import kotlinx.io.files.Path
import noorg.kloud.vthelper.applicationContext

actual fun dataDirectory(appId: String): Path = applicationContext.applicationInfo.dataDir.toPath()

actual fun cacheDirectory(appId: String): Path = applicationContext.cacheDir.absolutePath.toPath()