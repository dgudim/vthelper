package noorg.kloud.vthelper.data.data_providers

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.io.files.Path
import noorg.kloud.vthelper.aesDecrypt
import noorg.kloud.vthelper.aesEncrypt
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.api.MoodleApi
import noorg.kloud.vthelper.api.VTBaseApi
import noorg.kloud.vthelper.api.downloadFile
import noorg.kloud.vthelper.api.models.toResultOk
import noorg.kloud.vthelper.data.dbdaos.LoggedInUserDao
import noorg.kloud.vthelper.data.dbentities.DBLoggedInUserEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedLoggedInUserEntity
import noorg.kloud.vthelper.platform_specific.appDataDirectory
import noorg.kloud.vthelper.platform_specific.div

class LoggedInUserProvider(
    private val loggedInUserDao: LoggedInUserDao
) {

    val appDataDir = appDataDirectory()

    suspend fun logout() {
        loggedInUserDao.deleteAll()
        VTBaseApi.logout()
    }

    suspend fun login(
        studentId: String,
        plainPassword: String,
        mfaCode: String
    ): Result<String> {

        ManoApi.loginIfNeeded(studentId, plainPassword, mfaCode)
            .onFailure { return toResultFail() }

        MoodleApi.loginIfNeeded(studentId, plainPassword, mfaCode)
            .onFailure { return toResultFail() }

        return fetchUserDataFromApi(studentId, plainPassword)
    }

    private suspend fun fetchUserDataFromApi(
        studentId: String, plainPassword: String
    ): Result<String> {
        val studentInfoResult = ManoApi.getStudentInfo(::fetchUserDataFromApi.name)
            .onFailure { return toResultFail() }

        MoodleApi.updateSessionInfo()
            .onFailure { return toResultFail() }

        val studentInfo = studentInfoResult.bodyTyped!!
        val moodleUserId = MoodleApi.userId

        var avatarPath: Path? = null

        if(studentInfo.avatarUrl != null) {
            avatarPath = appDataDir / "$studentId.img"
            downloadFile(avatarPath, Url(studentInfo.avatarUrl))
        }

        // TODO: Save new cookies into the db after session refresh as well
        loggedInUserDao.replace(
            DBLoggedInUserEntity(
                studentId = studentId,
                passwordAes = plainPassword.aesEncrypt(),
                moodleId = moodleUserId.toString(),
                isSessionValid = true,
                universityEmail = studentInfo.universityEmail,
                personalEmail = studentInfo.personalEmail,
                fullName = studentInfo.fullName,
                phone = studentInfo.phone,
                cookiesJsonAes = VTBaseApi.cookieStorage.getAllAsJson().aesEncrypt(),
                address = studentInfo.address,
                birthDate = studentInfo.birthDate,
                avatarPath = avatarPath?.toString()
            )
        )

        return "OK".toResultOk()
    }

    fun getCurrentUserInfo(): Flow<ProvidedLoggedInUserEntity> {
        return loggedInUserDao
            .getAllAsFlow()
            .distinctUntilChanged()
            .map {
                val el = it.firstOrNull() ?: return@map ProvidedLoggedInUserEntity()
                with(el) {
                    ProvidedLoggedInUserEntity(
                        phone = phone,
                        address = address,
                        personalEmail = personalEmail,
                        universityEmail = universityEmail,

                        moodleId = moodleId,

                        studentId = studentId,
                        plainPassword = passwordAes.aesDecrypt(),
                        fullName = fullName,
                        birthDate = birthDate,
                        avatarPath = avatarPath,

                        plainCookiesJson = cookiesJsonAes.aesDecrypt(),
                        isSessionValid = isSessionValid
                    )
                }
            }
    }

}