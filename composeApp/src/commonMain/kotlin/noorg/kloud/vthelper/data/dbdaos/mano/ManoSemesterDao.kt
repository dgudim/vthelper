package noorg.kloud.vthelper.data.dbdaos.mano

import androidx.room.Dao
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

    @Query("SELECT * FROM mano_semesters ORDER BY absolute_seq desc")
    fun getAllAsFlow(): Flow<List<DBManoSemesterEntity>>

    // Last one is treated as latest for simplicity’s sake
    @Query("""SELECT * FROM mano_semesters
                        ORDER BY absolute_seq desc
                        LIMIT 1""")
    fun getCurrentAsFlow(): Flow<List<DBManoSemesterEntity>>


    @Query("""SELECT * FROM mano_semesters
                        ORDER BY absolute_seq desc
                        LIMIT 1""")
    suspend fun getCurrent(): List<DBManoSemesterEntity>
}