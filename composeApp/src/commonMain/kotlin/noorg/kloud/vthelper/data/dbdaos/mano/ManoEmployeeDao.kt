package noorg.kloud.vthelper.data.dbdaos.mano

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeEntity

@Dao
interface ManoEmployeeDao {
    @Upsert
    suspend fun upsert(item: DBManoEmployeeEntity)

    @Upsert
    suspend fun upsertMany(items: List<DBManoEmployeeEntity>)

    @Query("SELECT * from mano_employees where id = :id")
    suspend fun getById(id: String): DBManoEmployeeEntity?

    @Query("SELECT count(*) FROM mano_employees")
    suspend fun count(): Int

    @Query("SELECT * FROM mano_employees")
    fun getAllAsFlow(): Flow<List<DBManoEmployeeEntity>>
}