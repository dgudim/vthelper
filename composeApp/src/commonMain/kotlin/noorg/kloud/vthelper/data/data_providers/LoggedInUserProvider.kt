package noorg.kloud.vthelper.data.data_providers

import noorg.kloud.vthelper.data.dbdaos.LoggedInUserDao
import noorg.kloud.vthelper.data.provider_models.ProvidedLoggedInUserEntity

class LoggedInUserProvider(private val moodleCourseDao: LoggedInUserDao) {

    suspend fun getCurrentUserInfo(): ProvidedLoggedInUserEntity {
        val dbUser = moodleCourseDao.get()
        if(dbUser != null) {
            return ProvidedLoggedInUserEntity(
                phone = dbUser.phone,
                address = dbUser.address,
                personalEmail = dbUser.personalEmail,
                universityEmail = dbUser.universityEmail,

                moodleId = dbUser.moodleId,

                studentId = dbUser.studentId,
                password = dbUser.password,
                fullName = dbUser.fullName,
                birthDate = dbUser.birthDate,
                avatarPath = dbUser.avatarPath,

                cookiesJson = dbUser.cookiesJson,
                isSessionValid = dbUser.isSessionValid
            )
        }
        return ProvidedLoggedInUserEntity()
    }

}