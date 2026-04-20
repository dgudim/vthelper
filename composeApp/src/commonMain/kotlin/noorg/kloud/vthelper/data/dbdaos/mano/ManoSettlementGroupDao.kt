package noorg.kloud.vthelper.data.dbdaos.mano

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSettlementGroup
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSettlementGroupWithGrades
import noorg.kloud.vthelper.data.dbentities.mano.DbManoSettlementGrade
import noorg.kloud.vthelper.data.dbentities.mano.DbManoSettlementGradeWithEmployee

@Dao
interface ManoSettlementGroupDao {

    @Upsert
    suspend fun upsert(item: DBManoSettlementGroup)

    @Upsert
    suspend fun upsertMany(items: List<DBManoSettlementGroup>)

    @Query("SELECT count(*) FROM mano_settlement_groups")
    suspend fun count(): Int

    @Transaction
    @Query("SELECT * FROM mano_settlement_groups")
    fun getAllAsFlow(): Flow<List<DBManoSettlementGroup>>

    @Transaction
    @Query("SELECT * FROM mano_settlement_groups where subject_mod_id = :subjectModId")
    fun getForSubjectWithGrades(subjectModId: Int): Flow<List<DBManoSettlementGroupWithGrades>>

}