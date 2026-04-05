package noorg.kloud.vthelper.data.data_providers

import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.yield
import noorg.kloud.vthelper.api.models.moodle.ApiMoodleListCoursesResponse
import noorg.kloud.vthelper.data.dbdaos.MoodleCourseDao
import noorg.kloud.vthelper.data.dbentities.DBMoodleCourseEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedMoodleCourseEntity
import kotlin.Long

class MoodleCoursesProvider(private val moodleCourseDao: MoodleCourseDao) {

    suspend fun insertCoursesFromApi(courses: ApiMoodleListCoursesResponse) {
        moodleCourseDao.insertMany(
            courses
                .flatMap { apiCoursesResp -> apiCoursesResp.data.courses }
                .map { course ->
                    DBMoodleCourseEntity(
                        moodleId = course.id,
                        title = course.shortName,
                        description = course.summary,
                        coverImagePath = "" // TODO: Save to FS
                    )
                })
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