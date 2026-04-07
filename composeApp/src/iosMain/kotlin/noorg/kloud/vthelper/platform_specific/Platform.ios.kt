package noorg.kloud.vthelper

import platform.Foundation.NSProcessInfo
import platform.posix.TARGET_OS_SIMULATOR
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
fun CpuArchitecture.asArch(): CpuArch = when (this) {
    CpuArchitecture.UNKNOWN -> CpuArch.UNKNOWN
    CpuArchitecture.ARM32 -> CpuArch.ARM_X32
    CpuArchitecture.ARM64 -> CpuArch.ARM_X64
    CpuArchitecture.X86 -> CpuArch.X86
    CpuArchitecture.X64 -> CpuArch.X64
    else -> CpuArch.UNKNOWN
}

@OptIn(ExperimentalNativeApi::class)
actual fun platform(): OS {
    val nativePlatform = Platform

    val arch = nativePlatform.cpuArchitecture.asArch()
    val version = NSProcessInfo.processInfo.operatingSystemVersionString

    val isSimulator = TARGET_OS_SIMULATOR != 0

    return when (nativePlatform.osFamily) {
        OsFamily.MACOSX -> OS.MacOs(arch, version)
        OsFamily.IOS -> OS.IOS(arch, version, isSimulator)
        OsFamily.TVOS -> OS.TvOs(arch, version, isSimulator)
        OsFamily.WATCHOS -> OS.WatchOs(arch, version, isSimulator)
        else -> OS.Unknown(arch, version)
    }
}