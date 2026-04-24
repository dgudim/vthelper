package noorg.kloud.vthelper.data.provider_models

data class ProvidedManoEmployeeEntity (
    val manoId: Long,

    val avatarPath: String?,

    val fullName: String?,
    val shortName: String,

    val positions: String?,
    val departments: String?,
    val phones: String?,
    val emails: String?,
    val offices: String?
)