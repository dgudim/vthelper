package noorg.kloud.vthelper.data.data_providers

import androidx.compose.ui.graphics.Color
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import noorg.kloud.vthelper.api.MoodleApi
import noorg.kloud.vthelper.api.downloadFile
import noorg.kloud.vthelper.api.models.toResultOk
import noorg.kloud.vthelper.data.dbdaos.moodle.MoodleCourseDao
import noorg.kloud.vthelper.data.dbentities.moodle.DBMoodleCourseEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedMoodleCourseEntity
import noorg.kloud.vthelper.decodeBase64ToFile
import noorg.kloud.vthelper.getHashedColor
import noorg.kloud.vthelper.platform_specific.appDataDirectory
import noorg.kloud.vthelper.platform_specific.div

class MoodleCoursesProvider(private val moodleCourseDao: MoodleCourseDao) {

    val appDataDir = appDataDirectory()

    suspend fun fetchCoursesFromApi(): Result<String> {

        MoodleApi.getCourses()
            .onFailure { return toResultFail() }
            .onSuccess {
                moodleCourseDao.insertMany(
                    it
                        .flatMap { apiCoursesResp -> apiCoursesResp.data.courses }
                        .map { course ->
                            val coverImagePath = appDataDir / "moodle-${course.id}.img"

                            // TODO: Add a setting to download images only if not already downloaded
                            if (course.courseImageBase64OrUrl.startsWith("http")) {
                                downloadFile(coverImagePath, Url(course.courseImageBase64OrUrl))
                            } else {
                                course.courseImageBase64OrUrl.decodeBase64ToFile(coverImagePath)
                            }

                            DBMoodleCourseEntity(
                                moodleId = course.id,
                                title = course.fullName, // Specific purpose language culture (Academic writing)
                                description = course.shortName, // KILSB17027, V. Buivydienė (T/P), EN
                                viewUrl = course.viewUrl,
                                coverImagePath = coverImagePath.toString()
                            )
                        }
                )
            }

        return "OK".toResultOk()
    }

    fun getAllCourses(): Flow<List<ProvidedMoodleCourseEntity>> {
        return moodleCourseDao
            .getAllAsFlow()
            .distinctUntilChanged()
            .map { dbEntities ->
                dbEntities.map { dbEntity ->
                    println("Mapped moodle course: ${dbEntity.title}")
                    val modCode = dbEntity.description.split(',').first()
                    ProvidedMoodleCourseEntity(
                        moodleId = dbEntity.moodleId,
                        title = dbEntity.title,
                        description = dbEntity.description,
                        coverImagePath = dbEntity.coverImagePath,
                        viewUrl = dbEntity.viewUrl,
                        courseModCode = modCode,
                        color =
                            if (dbEntity.customColor == null)
                                getHashedColor(modCode.hashCode().toLong())
                            else
                                Color(dbEntity.customColor)
                    )
                }
            }
    }

}