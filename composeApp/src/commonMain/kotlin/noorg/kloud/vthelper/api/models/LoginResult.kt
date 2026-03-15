package noorg.kloud.vthelper.api.models

data class LoginResult(
    val statusCode: Int,
    val context: String,
    val responseContent: String,
    val isSuccessful: Boolean,
    val failStep: String
) {
    fun getFullStatus(): String {
        if (isSuccessful) {
            return "Completed successfully"
        }
        return "Failed at '$failStep' with status code '$statusCode'. Why: '$context'. Truncated content: '${
            responseContent.take(
                100
            )
        }...'"
    }
}