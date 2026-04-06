package noorg.kloud.vthelper.api

import noorg.kloud.vthelper.api.models.ApiResult

inline fun <T> safeApiCall(operation: String, block: (op: String) -> ApiResult<T>): ApiResult<T> {
    return runCatching {
        block(operation)
    }.run {
        if (isSuccess) {
            getOrNull()!!
        } else {
            ApiResult.fromException(exceptionOrNull()!!, operation)
        }
    }
}

/** Retry if the error was marked directly in the model somehow, see 'successUpdater' in [ApiResult] */
inline fun <T, R> safeRetryOnDirectApiError(
    operation: String,
    retryOperation: String,
    mainBlock: (op: String) -> ApiResult<T>,
    beforeRetryBlock: (op: String) -> ApiResult<R>
): ApiResult<T> {
    val result = safeApiCall(operation) { mainBlock(it) }
    // Model was deserialized successfully but the result is still failed -> Error in the model
    if (!result.isSuccess && result.bodyTyped != null) {
        val retryResult =
            safeApiCall("$retryOperation (retry before '$operation')") { beforeRetryBlock(it) }
        if (!retryResult.isSuccess) {
            // 'Before retry' failed
            return ApiResult.fromOtherResult(retryResult)
        }
        // Success of fail after second call
        return safeApiCall("$operation (2nd attempt)") { mainBlock(it) }
    }
    // Successful
    return result
}