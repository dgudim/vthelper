package noorg.kloud.vthelper.data.provider_models

data class ProvidedManoEmployeeEntity (
    val manoId: Long,

    val fullName: String? = null,
    val shortName: String? = null,

    val positions: String? = null,
    val departments: String? = null,
    val phones: String? = null,
    val emails: String? = null,
    val offices: String? = null
)