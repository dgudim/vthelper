package noorg.kloud.vthelper.data.dbdaos.mano

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSemesterEntity

@Dao
interface ManoSemesterDao {
    @Upsert
    suspend fun upsert(item: DBManoSemesterEntity)

    @Upsert
    suspend fun upsertMany(items: List<DBManoSemesterEntity>)

    @Query("SELECT count(*) FROM mano_semesters")
    suspend fun count(): Int

    @Query("SELECT * FROM mano_semesters order by seq desc")
    fun getAllAsFlow(): Flow<List<DBManoSemesterEntity>>
}