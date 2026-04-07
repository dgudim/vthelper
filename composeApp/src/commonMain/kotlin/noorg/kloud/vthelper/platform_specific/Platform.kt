package noorg.kloud.vthelper.platform_specific


enum class CpuArch {
    UNKNOWN,
    X64,
    X86,
    ARM_X64,
    ARM_X32,
}

sealed class OS(open val cpuArch: CpuArch) {
    data class Unknown(override val cpuArch: CpuArch, val version: String) : OS(cpuArch)

    data class MacOs(override val cpuArch: CpuArch, val version: String) : OS(cpuArch)

    data class IOS(override val cpuArch: CpuArch, val version: String, val isSimulator: Boolean) : OS(cpuArch)

    data class WatchOs(override val cpuArch: CpuArch, val version: String, val isSimulator: Boolean) : OS(cpuArch)

    data class TvOs(override val cpuArch: CpuArch, val version: String, val isSimulator: Boolean) : OS(cpuArch)

    data class Android(
        override val cpuArch: CpuArch,
        val buildNumber: Int,
        val androidVersion: String,
        val isWatch: Boolean,
        val isTv: Boolean,
    ) : OS(cpuArch)

    data class Linux(override val cpuArch: CpuArch, val version: String) : OS(cpuArch)

    data class Windows(override val cpuArch: CpuArch, val version: String) : OS(cpuArch)
}

expect fun platform(): OS