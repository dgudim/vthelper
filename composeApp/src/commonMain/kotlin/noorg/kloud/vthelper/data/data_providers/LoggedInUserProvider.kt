package noorg.kloud.vthelper.data.data_providers

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.api.MoodleApi
import noorg.kloud.vthelper.api.VTBaseApi
import noorg.kloud.vthelper.api.downloadImage
import noorg.kloud.vthelper.data.dbdaos.LoggedInUserDao
import noorg.kloud.vthelper.data.dbentities.DBLoggedInUserEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedLoggedInUserEntity
import noorg.kloud.vthelper.platform_specific.appDataDirectory
import noorg.kloud.vthelper.platform_specific.div

class LoggedInUserProvider(
    private val loggedInUserDao: LoggedInUserDao
) {

    suspend fun logout() {
        loggedInUserDao.deleteAll()
        VTBaseApi.logout()
    }

    suspend fun login(
        studentId: String,
        password: String,
        mfaCode: String
    ): Result<String> {
        val manoResult = ManoApi.loginIfNeeded(studentId, password, mfaCode)
        if (manoResult.isFailure) {
            return manoResult.toResult()
        }
        val moodleResult = MoodleApi.loginIfNeeded(studentId, password, mfaCode)
        if (moodleResult.isFailure) {
            return moodleResult.toResult()
        }
        val userdataResult = fetchUserDataFromApi(studentId, password)
        return userdataResult
    }

    private suspend fun fetchUserDataFromApi(
        studentId: String, password: String
    ): Result<String> {
        val studentInfoResult = ManoApi.getStudentInfo()
        if (studentInfoResult.isFailure) {
            return studentInfoResult.toResult()
        }

        val moodleUserIdResult = MoodleApi.updateSessionInfo()
        if (moodleUserIdResult.isFailure) {
            return studentInfoResult.toResult()
        }

        val studentInfo = studentInfoResult.bodyTyped!!
        val moodleUserId = MoodleApi.userId

        val avatarPath = appDataDirectory() / "$studentId.img"

        downloadImage(avatarPath, Url(studentInfo.avatarUrl))

        loggedInUserDao.insert(
            DBLoggedInUserEntity(
                studentId = studentId,
                password = password,
                moodleId = moodleUserId.toString(),
                isSessionValid = true,
                universityEmail = studentInfo.universityEmail,
                personalEmail = studentInfo.personalEmail,
                fullName = studentInfo.fullName,
                phone = studentInfo.phone,
                cookiesJson = VTBaseApi.cookieStorage.getAllAsJson(),
                address = studentInfo.address,
                birthDate = studentInfo.birthDate,
                avatarPath = avatarPath.toString()
            )
        )

        return Result.success("OK")
    }

    fun getCurrentUserInfo(): Flow<ProvidedLoggedInUserEntity> {
        return loggedInUserDao.getAllAsFlow().map {
            try {
                with(it.first()) {
                    ProvidedLoggedInUserEntity(
                        phone = phone,
                        address = address,
                        personalEmail = personalEmail,
                        universityEmail = universityEmail,

                        moodleId = moodleId,

                        studentId = studentId,
                        password = password,
                        fullName = fullName,
                        birthDate = birthDate,
                        avatarPath = avatarPath,

                        cookiesJson = cookiesJson,
                        isSessionValid = isSessionValid
                    )
                }
            } catch (_: NoSuchElementException) {
                ProvidedLoggedInUserEntity()
            }
        }
    }

}