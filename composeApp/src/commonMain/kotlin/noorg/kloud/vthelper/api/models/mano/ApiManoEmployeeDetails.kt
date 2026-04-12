package noorg.kloud.vthelper.api.models.mano

data class ApiManoBasicDepartmentData(
    val id: String,
    val name: String
)

data class ApiManoBasicOfficeData(
    val officeName: String,
    val address: String
)

// If there are multiple cards we bundle into one object
data class ApiManoEmployeeDetails(
    val fullNameWithPrefix: String, // 'Dr.' or similar
    val positions: List<String>,
    val departments: List<ApiManoBasicDepartmentData>,
    val phones: List<String>,
    val emails: List<String>,
    val offices: List<ApiManoBasicOfficeData>
)