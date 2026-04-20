package noorg.kloud.vthelper.data.dbdaos.mano

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.mano.DbManoSettlementGrade
import noorg.kloud.vthelper.data.dbentities.mano.DbManoSettlementGradeWithEmployee

@Dao
interface ManoSettlementGradeDao {

    @Upsert
    suspend fun upsert(item: DbManoSettlementGrade)

    @Upsert
    suspend fun upsertMany(items: List<DbManoSettlementGrade>)

    @Query("SELECT count(*) FROM mano_settlement_grades")
    suspend fun count(): Int

    @Transaction
    @Query("SELECT * FROM mano_settlement_grades")
    fun getAllAsFlow(): Flow<List<DbManoSettlementGrade>>

    @Transaction
    @Query("SELECT * FROM mano_settlement_grades where settlement_id = :settlementId")
    fun getForSettlementWithEmployee(settlementId: Int): Flow<List<DbManoSettlementGradeWithEmployee>>
}