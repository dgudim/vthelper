package noorg.kloud.vthelper.data.data_providers

import noorg.kloud.vthelper.data.dbdaos.mano.ManoSettlementGradeDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSettlementGroupDao

class ManoSettlementProvider(
    private val manoSettlementGroupDao: ManoSettlementGroupDao,
    private val manoSettlementGradeDao: ManoSettlementGradeDao
) {

    suspend fun fetchGradesForSubject() {

    }

}