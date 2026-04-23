package noorg.kloud.vthelper.data.data_providers

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.api.downloadImage
import noorg.kloud.vthelper.api.models.toResultFail
import noorg.kloud.vthelper.api.models.toResultOk
import noorg.kloud.vthelper.data.dbdaos.mano.ManoEmployeeDao
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeExtendedData
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeExtendedDataWithPk
import noorg.kloud.vthelper.data.provider_models.ProvidedManoEmployeeEntity
import noorg.kloud.vthelper.platform_specific.appDataDirectory
import noorg.kloud.vthelper.platform_specific.div
import kotlin.Long

class ManoEmployeeProvider(private val manoEmployeeDao: ManoEmployeeDao) {

    suspend fun fetchEmployeeListFromApi(): Result<List<DBManoEmployeeEntity>> {
        val employees = mutableListOf<DBManoEmployeeEntity>()

        // Insert a placeholder for the cases when the subject lecturer can't be mapped for some reason
        val dummy = DBManoEmployeeEntity(
            manoId = 0,
            shortName = "-",
            extendedData = DBManoEmployeeExtendedData()
        )
        employees.add(dummy)
        manoEmployeeDao.upsert(dummy)

        ManoApi.getEmployees()
            .onFailure { return toResultFail() }
            .onSuccess { result ->
                val fetchedEmployees =
                    result.map {
                        DBManoEmployeeEntity(
                            manoId = it.id,
                            shortName = it.shortName,
                            extendedData = DBManoEmployeeExtendedData()
                        )
                    }
                employees.addAll(fetchedEmployees)
                manoEmployeeDao.upsertMany(fetchedEmployees)
            }

        return employees.toResultOk()
    }

    suspend fun fetchEmployeeDetailsFromApi(employeeId: Long): Result<String> {

        ManoApi.getEmployeeDetails(employeeId)
            .onFailure { return toResultFail() }
            .onSuccess { resp ->
                val avatarPath = appDataDirectory() / "employee-$employeeId.img"
                downloadImage(avatarPath, Url(resp.avatarUrl))

                manoEmployeeDao.updateExtended(
                    DBManoEmployeeExtendedDataWithPk(
                        manoId = employeeId,
                        extendedData = DBManoEmployeeExtendedData(
                            fullName = resp.fullNameWithPrefix,
                            emails = resp.emails.joinToString(", "),
                            phones = resp.phones.joinToString(", "),
                            positions = resp.positions.joinToString(", "),
                            offices = resp.offices.joinToString(", ") { "${it.officeName} (${it.address})" },
                            departments = resp.departments.joinToString(", ") { it.name },
                            avatarPath = avatarPath.toString()
                        )
                    )
                )
            }

        return "OK".toResultOk()
    }

    private fun mapDbToProvider(model: DBManoEmployeeEntity): ProvidedManoEmployeeEntity {
        println("Mapped employee: ${model.shortName}")
        with(model) {
            return ProvidedManoEmployeeEntity(
                manoId = manoId,
                fullName = extendedData.fullName,
                shortName = shortName,
                positions = extendedData.positions,
                departments = extendedData.departments,
                phones = extendedData.phones,
                emails = extendedData.emails,
                offices = extendedData.offices,
            )
        }
    }

    fun getAllEmployees(): Flow<List<ProvidedManoEmployeeEntity>> {
        return manoEmployeeDao
            .getAllAsFlow()
            .distinctUntilChanged()
            .map { dbEntities ->
                dbEntities.map { mapDbToProvider(it) }
            }
    }

    fun getEmployeeById(employeeId: Long): Flow<ProvidedManoEmployeeEntity?> {
        return manoEmployeeDao
            .getByIdAsFlow(employeeId)
            .distinctUntilChanged()
            .map {
                return@map if (it != null) {
                    mapDbToProvider(it)
                } else {
                    null
                }
            }
    }

}