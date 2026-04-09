package noorg.kloud.vthelper.data.data_providers

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import noorg.kloud.vthelper.api.MoodleApi
import noorg.kloud.vthelper.api.models.moodle.ApiMoodleListCoursesResponse
import noorg.kloud.vthelper.data.dbdaos.MoodleCourseDao
import noorg.kloud.vthelper.data.dbentities.DBMoodleCourseEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedMoodleCourseEntity
import noorg.kloud.vthelper.decodeBase64ToFile
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
                    course.courseImageBase64.decodeBase64ToFile(coverImagePath)

                    DBMoodleCourseEntity(
                        moodleId = course.id,
                        title = course.shortName,
                        description = course.summary,
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
                        color = Color.Red // TODO: Fetch from settings
                    )
                }
            }
    }

}