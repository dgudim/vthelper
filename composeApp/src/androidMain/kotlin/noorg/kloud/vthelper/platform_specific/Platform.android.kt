package noorg.kloud.vthelper.platform_specific

import android.content.pm.PackageManager
import android.os.Build
import noorg.kloud.vthelper.applicationContext

actual fun platform(): OS {
    return OS.Android(
        cpuArch = arch(),
        buildNumber = Build.VERSION.SDK_INT,
        androidVersion = Build.VERSION.RELEASE,
        isWatch = applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH),
        isTv = applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK_ONLY),
    )
}

private fun arch(): CpuArch {
    Build.SUPPORTED_ABIS.orEmpty().forEach {
        when (it) {
            "arm64-v8a" -> return CpuArch.ARM_X64
            "armeabi-v7a" -> return CpuArch.ARM_X64
            "x86_64" -> return CpuArch.X64
            "x86" -> return CpuArch.X86
        }
    }
    return CpuArch.UNKNOWN
}