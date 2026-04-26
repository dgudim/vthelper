package noorg.kloud.vthelper.data.dbdaos.mano

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEntityWithEmployee

@Dao
interface ManoSubjectDao {
    @Upsert
    suspend fun upsert(item: DBManoSubjectEntity)

    @Upsert
    suspend fun upsertMany(items: List<DBManoSubjectEntity>)

    @Query("SELECT count(*) FROM mano_subjects")
    suspend fun count(): Int

    @Transaction
    @Query("SELECT * FROM mano_subjects ORDER BY name ASC")
    fun getAllWithEmployeeAsFlow(): Flow<List<DBManoSubjectEntityWithEmployee>>

    @Transaction
    @Query(
        """SELECT * FROM mano_subjects 
                         WHERE semester_absolute_seq = :semesterAbsoluteSequence
                         ORDER BY name ASC"""
    )
    fun getForSemesterWithEmployee(semesterAbsoluteSequence: Int): Flow<List<DBManoSubjectEntityWithEmployee>>
}