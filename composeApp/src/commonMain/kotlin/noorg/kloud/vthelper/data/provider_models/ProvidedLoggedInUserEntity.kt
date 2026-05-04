package noorg.kloud.vthelper.data.provider_models

data class ProvidedLoggedInUserEntity (

    val studentId: String? = null,
    val plainPassword: String? = null,

    val moodleId: String? = null,

    val isSessionValid: Boolean = false,

    val personalEmail: String? = null,
    val universityEmail: String? = null,
    val phone: String? = null,

    val address: String? = null,
    val birthDate: String? = null,

    val fullName: String? = null,
    val avatarPath: String? = null,

    val plainCookiesJson: String? = null,
)