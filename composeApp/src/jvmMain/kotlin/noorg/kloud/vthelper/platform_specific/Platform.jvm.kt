package noorg.kloud.vthelper.platform_specific

enum class DesktopOs {
    Macos,
    Linux,
    Windows,
    Unknown,
}

internal fun hostArch(archName: String) = when (archName) {
    "i486", "i386", "i586", "i686", "x86" -> CpuArch.X86
    "x86_64", "amd64" -> CpuArch.X64
    "aarch_64", "arm64", "aarch64" -> CpuArch.ARM_X64
    "arm32" -> CpuArch.ARM_X32
    else -> CpuArch.UNKNOWN
}

private fun hostOs(name: String) = name.lowercase().let { osName ->
    when {
        osName.startsWith("mac") ||
                osName.startsWith("osx") ||
                osName.startsWith("darwin") -> DesktopOs.Macos

        osName.startsWith("win") -> DesktopOs.Windows
        osName.startsWith("linux") -> DesktopOs.Linux
        else -> DesktopOs.Unknown
    }
}

internal fun hostOs(osName: String, archName: String, version: String): OS {
    val arch = hostArch(archName)
    return when (hostOs(osName)) {
        DesktopOs.Macos -> OS.MacOs(arch, version)
        DesktopOs.Linux -> OS.Linux(arch, version)
        DesktopOs.Windows -> OS.Windows(arch, version)
        DesktopOs.Unknown -> OS.Unknown(arch, version)
    }
}

val hostOs: OS = hostOs(
    osName = System.getProperty("os.name"),
    archName = System.getProperty("os.arch"),
    version = System.getProperty("os.version"),
)

actual fun platform(): OS = hostOs