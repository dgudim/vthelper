package noorg.kloud.vthelper.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.entities.MoodleCourseEntity

@Dao
interface MoodleCourseDao {
    @Insert
    suspend fun insert(item: MoodleCourseEntity)

    @Query("SELECT count(*) FROM moodle_courses")
    suspend fun count(): Int

    @Query("SELECT * FROM moodle_courses")
    fun getAllAsFlow(): Flow<List<MoodleCourseEntity>>
}