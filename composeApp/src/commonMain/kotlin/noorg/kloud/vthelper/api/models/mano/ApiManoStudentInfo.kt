package noorg.kloud.vthelper.api.models.mano

import noorg.kloud.vthelper.api.models.ApiResult

data class ApiManoStudentInfo (
    val fullName: String,
    val birthYear: Int,
    val birthDate: String,
    val address: String,
    val phone: String,
    val personalEmail: String,
    val universityEmail: String,
    val avatarUrl: String
)

fun ApiManoStudentInfo.toApiResult(
    operation: String
): ApiResult<ApiManoStudentInfo> {
    return ApiResult.fromDeserializedModel(this, operation)
}