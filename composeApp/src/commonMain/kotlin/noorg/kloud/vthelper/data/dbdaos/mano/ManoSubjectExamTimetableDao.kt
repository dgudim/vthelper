package noorg.kloud.vthelper.data.dbdaos.mano

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.mano.DbManoSubjectExamTimetableEntity

@Dao
interface ManoSubjectExamTimetableDao {

    @Insert
    suspend fun insertMany(items: List<DbManoSubjectExamTimetableEntity>)

    @Query("delete from mano_exam_timetable")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(items: List<DbManoSubjectExamTimetableEntity>) {
        deleteAll()
        insertMany(items)
    }

    @Query("SELECT * FROM mano_exam_timetable")
    fun getAllAsFlow(): Flow<List<DbManoSubjectExamTimetableEntity>>

}