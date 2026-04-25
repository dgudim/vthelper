package noorg.kloud.vthelper.data.dbdaos.mano

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.mano.DBManoBareEmployeeData
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeExtendedDataWithPk

@Dao
interface ManoEmployeeDao {
    @Upsert(entity = DBManoEmployeeEntity::class)
    suspend fun upsertBare(item: DBManoBareEmployeeData)

    @Upsert(entity = DBManoEmployeeEntity::class)
    suspend fun upsertBareMany(items: List<DBManoBareEmployeeData>)

    @Update(entity = DBManoEmployeeEntity::class)
    suspend fun updateExtended(employee: DBManoEmployeeExtendedDataWithPk)

    @Query("SELECT * from mano_employees where mano_id = :id")
    suspend fun getById(id: Long): DBManoEmployeeEntity?

    @Query("SELECT count(*) FROM mano_employees")
    suspend fun count(): Int

    @Query("SELECT * FROM mano_employees")
    fun getAllAsFlow(): Flow<List<DBManoEmployeeEntity>>

    @Query("SELECT * FROM mano_employees")
    suspend fun getAll(): List<DBManoEmployeeEntity>
}