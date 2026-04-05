package noorg.kloud.vthelper.data.dbdaos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.DBMoodleCourseEntity

@Dao
interface MoodleCourseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMany(items: List<DBMoodleCourseEntity>)

    @Query("SELECT count(*) FROM moodle_courses")
    suspend fun count(): Int

    @Query("SELECT * FROM moodle_courses")
    fun getAllAsFlow(): Flow<List<DBMoodleCourseEntity>>
}