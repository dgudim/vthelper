package noorg.kloud.vthelper.api

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import io.ktor.utils.io.asByteWriteChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import noorg.kloud.vthelper.api.models.NetResult

inline fun <T> safeNetCall(operation: String, block: (op: String) -> NetResult<T>): NetResult<T> {
    return runCatching {
        block(operation)
    }.getOrElse {
        NetResult.fromException(it, operation)
    }
}

/** Retry if the error was marked directly in the model somehow, see 'successUpdater' in [NetResult] */
inline fun <T, R> safeRetryOnDirectApiError(
    operation: String,
    retryOperation: String,
    mainBlock: (op: String) -> NetResult<T>,
    beforeRetryBlock: (op: String) -> NetResult<R>
): NetResult<T> {
    val result = safeNetCall(operation) { mainBlock(it) }
    // Model was deserialized successfully but the result is still failed -> Error in the model
    if (!result.isSuccess && result.bodyTyped != null) {
        val retryResult =
            safeNetCall("$retryOperation (retry before '$operation')") { beforeRetryBlock(it) }
        if (!retryResult.isSuccess) {
            // 'Before retry' failed
            return NetResult.fromOtherResult(retryResult)
        }
        // Success of fail after second call
        return safeNetCall("$operation (2nd attempt)") { mainBlock(it) }
    }
    // Successful
    return result
}

suspend fun downloadImage(target: Path, sourceUrl: Url): NetResult<String> {
    return safeNetCall("download $sourceUrl") {
        VTBaseApi.client
            .get(sourceUrl)
            .bodyAsChannel()
            .copyAndClose(SystemFileSystem.sink(target, false).asByteWriteChannel())

        NetResult.fromDeserializedModel("OK", it)
    }
}