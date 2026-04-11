package noorg.kloud.vthelper.data.dbdaos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.DBManoSubjectTimetableEntity

@Dao
interface ManoSubjectTimetableDao {
    @Insert
    suspend fun insert(item: DBManoSubjectTimetableEntity)

    @Query("SELECT count(*) FROM mano_timetable")
    suspend fun count(): Int

    @Query("SELECT * FROM mano_timetable")
    fun getAllAsFlow(): Flow<DBManoSubjectTimetableEntity>
}