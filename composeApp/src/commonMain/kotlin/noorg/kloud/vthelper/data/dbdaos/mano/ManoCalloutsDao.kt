package noorg.kloud.vthelper.data.dbdaos.mano

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSettlementGroup
import noorg.kloud.vthelper.data.dbentities.mano.DbManoCalloutEntity

@Dao
interface ManoCalloutsDao {

    @Insert
    suspend fun insertMany(items: List<DbManoCalloutEntity>)

    @Query("delete from mano_callouts")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(items: List<DbManoCalloutEntity>) {
        deleteAll()
        insertMany(items)
    }

    @Query("SELECT * FROM mano_callouts")
    fun getAllAsFlow(): Flow<List<DbManoCalloutEntity>>
}