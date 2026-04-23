package noorg.kloud.vthelper.api.models.mano.grades

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiManoSemesterMediateResults(
    val datas: ApiManoSemesterMediateResultsData,
)

@Serializable
data class ApiManoSemesterMediateResultsData(
    val results: List<ApiManoSubjectMediateResults>
)

@Serializable
data class ApiManoSubjectMediateResults(
    @SerialName("ID_STD_TRS")
    val stdTrsId: String, // 1171020105
    @SerialName("ID_MOD")
    val modId: String, // 25891222
    @SerialName("MOD_KODAS")
    val modCode: String, // KILSB17027
    @SerialName("MODULIS")
    val name: String, // Specific Purpose Language Culture (KILSB17027)
    @SerialName("DESTYTOJAI")
    val lecturerName: String, // V. Buivydienė
    @SerialName("SESIJA_SKST")
    val sessionSkst: String, // TODO: What is this? It's always "1"
    @SerialName("SESIJOS_PAV")
    val season: String, // Autumn semester
    @SerialName("MOKSLO_METAI")
    val yearRange: String, // 2025-2026
    @SerialName("IVERTINIMO_DALIS")
    val taGaSplitPercentage: String, // 50/50
    @SerialName("SURINKTA")
    val cumulativeScore: String, // 4,30
    @SerialName("GA_BALAS")
    val fullCreditAndScore: String // 8 (4,00+1)
)