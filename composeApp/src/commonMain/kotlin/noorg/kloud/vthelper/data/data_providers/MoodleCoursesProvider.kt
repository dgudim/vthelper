package noorg.kloud.vthelper.data.data_providers

import androidx.compose.ui.graphics.Color
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.io.files.SystemFileSystem
import noorg.kloud.vthelper.api.MoodleApi
import noorg.kloud.vthelper.api.downloadImage
import noorg.kloud.vthelper.data.dbdaos.MoodleCourseDao
import noorg.kloud.vthelper.data.dbentities.DBMoodleCourseEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedMoodleCourseEntity
import noorg.kloud.vthelper.decodeBase64ToFile
import noorg.kloud.vthelper.getHashedColor
import noorg.kloud.vthelper.platform_specific.appDataDirectory
import noorg.kloud.vthelper.platform_specific.div

class MoodleCoursesProvider(private val moodleCourseDao: MoodleCourseDao) {

    val appDataDir = appDataDirectory()

    suspend fun fetchCoursesFromApi(): Result<String> {
        val coursesResponse = MoodleApi.getCourses()

        if (coursesResponse.isFailure) {
            return coursesResponse.toResult()
        }

        moodleCourseDao.insertMany(
            coursesResponse.bodyTyped!!
                .flatMap { apiCoursesResp -> apiCoursesResp.data.courses }
                .map { course ->
                    val coverImagePath = appDataDir / "moodle-${course.id}.img"

                    // TODO: Add a setting to download images only if not already downloaded
                    if (course.courseImageBase64OrUrl.startsWith("http")) {
                        downloadImage(coverImagePath, Url(course.courseImageBase64OrUrl))
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
                })

        return coursesResponse.toResult()
    }

    fun getAllCourses(): Flow<List<ProvidedMoodleCourseEntity>> {
        return moodleCourseDao.getAllAsFlow()
            .map { dbEntities ->
                dbEntities.map { dbEntity ->
                    println("Mapped moodle course: ${dbEntity.title}")
                    ProvidedMoodleCourseEntity(
                        moodleId = dbEntity.moodleId,
                        title = dbEntity.title,
                        description = dbEntity.description,
                        coverImagePath = dbEntity.coverImagePath,
                        viewUrl = dbEntity.viewUrl,
                        color = getHashedColor(
                            dbEntity.moodleId
                        ) // TODO: Fetch from settings
                    )
                }
            }
    }

}