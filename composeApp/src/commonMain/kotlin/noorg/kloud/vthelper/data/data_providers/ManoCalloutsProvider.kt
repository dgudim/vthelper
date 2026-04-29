package noorg.kloud.vthelper.data.data_providers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.api.models.toResultOk
import noorg.kloud.vthelper.data.dbdaos.mano.ManoCalloutsDao
import noorg.kloud.vthelper.data.dbentities.mano.DbManoCalloutEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoCalloutEntity

class ManoCalloutsProvider(private val manoCalloutsDao: ManoCalloutsDao) {

    suspend fun fetchAllCallouts(): Result<String> {
        ManoApi.getCallouts(::fetchAllCallouts.name)
            .onFailure { return toResultFail() }
            .onSuccess {
                manoCalloutsDao.replaceAll(it.map { el ->
                    DbManoCalloutEntity(
                        type = el.type,
                        contents = el.contents
                    )
                })
            }

        return "OK".toResultOk()
    }

    fun getAllCalloutsAsFlow(): Flow<List<ProvidedManoCalloutEntity>> {
        return manoCalloutsDao
            .getAllAsFlow()
            .distinctUntilChanged()
            .map { dbEntities ->
                dbEntities.map {
                    ProvidedManoCalloutEntity(
                        type = it.type,
                        contents = it.contents
                    )
                }
            }
    }
}