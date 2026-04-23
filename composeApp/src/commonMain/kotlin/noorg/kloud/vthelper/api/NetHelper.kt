package noorg.kloud.vthelper.api

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import io.ktor.utils.io.asByteWriteChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import noorg.kloud.vthelper.api.models.NetResult
import noorg.kloud.vthelper.api.models.toNetResultOk

inline fun <T> safeNetCall(operation: String, block: (op: String) -> NetResult<T>): NetResult<T> {
    return runCatching {
        block(operation)
    }.getOrElse {
        NetResult.fromException(it, operation)
    }
}

/** Retry if the error was marked directly in the model somehow, see 'successUpdater' in [NetResult] */
inline fun <T, R> safeRetryWithPrecall(
    operation: String,
    retryOperation: String,
    mainBlock: (op: String) -> NetResult<T>,
    beforeRetryBlock: (op: String, mainBlockResult: NetResult<T>) -> NetResult<R>
): NetResult<T> {
    val result = safeNetCall(operation) { mainBlock(it) }
    if (!result.isSuccess) {
        val beforeRetryResult =
            safeNetCall("$retryOperation (retry before '$operation')") {
                beforeRetryBlock(
                    it,
                    result
                )
            }
        if (!beforeRetryResult.isSuccess) {
            // 'Before retry' failed
            return NetResult.fromOtherResult(beforeRetryResult)
        }
        // Success of fail after second call
        return safeNetCall("$operation (2nd attempt)") { mainBlock(it) }
    }
    // Successful
    return result
}

inline fun <T> safeRetry(
    operation: String,
    nRetries: Int,
    mainBlock: (op: String, previousResult: NetResult<T>?) -> NetResult<T>
): NetResult<T> {
    var prevResult: NetResult<T>? = null
    for (i in 1..nRetries) {
        prevResult = safeNetCall("$operation (attempt No $i)") { mainBlock(it, prevResult) }
        if (prevResult.isSuccess) {
            return prevResult
        }
    }
    return prevResult!!
}

suspend fun downloadImage(target: Path, sourceUrl: Url): NetResult<String> {
    return safeNetCall("download $sourceUrl") {
        VTBaseApi.client
            .get(sourceUrl)
            .bodyAsChannel()
            .copyAndClose(
                SystemFileSystem.sink(target, false)
                    .asByteWriteChannel()
            )

        "Downloaded $sourceUrl".toNetResultOk(it)
    }
}