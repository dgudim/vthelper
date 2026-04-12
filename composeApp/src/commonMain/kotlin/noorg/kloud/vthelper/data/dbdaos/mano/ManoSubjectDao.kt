package noorg.kloud.vthelper.data.dbdaos.mano

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEntity

@Dao
interface ManoSubjectDao {
    @Upsert
    suspend fun upsert(item: DBManoSubjectEntity)

    @Upsert
    suspend fun upsertMany(items: List<DBManoSubjectEntity>)

    @Query("SELECT count(*) FROM mano_subjects")
    suspend fun count(): Int

    @Query("SELECT * FROM mano_subjects")
    fun getAllAsFlow(): Flow<List<DBManoSubjectEntity>>
}