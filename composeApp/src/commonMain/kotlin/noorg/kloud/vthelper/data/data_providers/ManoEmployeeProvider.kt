package noorg.kloud.vthelper.data.data_providers

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.io.files.Path
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.api.downloadImage
import noorg.kloud.vthelper.api.models.toResultOk
import noorg.kloud.vthelper.data.dbdaos.mano.ManoEmployeeDao
import noorg.kloud.vthelper.data.dbentities.mano.DBManoBareEmployeeData
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeExtendedData
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeExtendedDataWithPk
import noorg.kloud.vthelper.data.provider_models.ProvidedManoEmployeeEntity
import noorg.kloud.vthelper.nullIfBlank
import noorg.kloud.vthelper.platform_specific.appDataDirectory
import noorg.kloud.vthelper.platform_specific.div
import kotlin.Long

class ManoEmployeeProvider(private val manoEmployeeDao: ManoEmployeeDao) {

    suspend fun fetchEmployeeListFromApi(): Result<List<DBManoBareEmployeeData>> {
        val employees = mutableListOf<DBManoBareEmployeeData>()

        // Insert a placeholder for the cases when the subject lecturer can't be mapped for some reason
        val dummy = DBManoBareEmployeeData(
            manoId = 0,
            shortName = "-"
        )
        employees.add(dummy)
        manoEmployeeDao.upsertBare(dummy)

        ManoApi.getEmployees()
            .onFailure { return toResultFail() }
            .onSuccess { result ->
                val fetchedEmployees =
                    result.map {
                        DBManoBareEmployeeData(
                            manoId = it.id,
                            shortName = it.shortName
                        )
                    }
                employees.addAll(fetchedEmployees)
                manoEmployeeDao.upsertBareMany(fetchedEmployees)
            }

        return employees.toResultOk()
    }

    suspend fun fetchEmployeeDetailsFromApi(employeeId: Long): Result<String> {

        ManoApi.getEmployeeDetails(employeeId)
            .onFailure { return toResultFail() }
            .onSuccess { resp ->

                var avatarPath: Path? = null

                if(resp.avatarUrl != null) {
                    avatarPath = appDataDirectory() / "employee-$employeeId.img"
                    downloadImage(avatarPath, Url(resp.avatarUrl))
                }

                manoEmployeeDao.updateExtended(
                    DBManoEmployeeExtendedDataWithPk(
                        manoId = employeeId,
                        extendedData = DBManoEmployeeExtendedData(
                            fullName = resp.fullNameWithPrefix,
                            emails = resp.emails.joinToString(", ").nullIfBlank(),
                            phones = resp.phones.joinToString(", ").nullIfBlank(),
                            positions = resp.positions.joinToString(", ").nullIfBlank(),
                            offices = resp.offices.joinToString(", ") { "${it.officeName} (${it.address})" }.nullIfBlank(),
                            departments = resp.departments.joinToString(", ") { it.name }.nullIfBlank(),
                            avatarPath = avatarPath?.toString()
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
                avatarPath = extendedData.avatarPath,
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

    fun getAllEmployeesAsFlow(): Flow<List<ProvidedManoEmployeeEntity>> {
        return manoEmployeeDao
            .getAllAsFlow()
            .distinctUntilChanged()
            .map { dbEntities ->
                dbEntities.map { mapDbToProvider(it) }
            }
    }

    suspend fun getEmployeeById(employeeId: Long): ProvidedManoEmployeeEntity? {
        val dbEntity = manoEmployeeDao.getById(employeeId)
        return if (dbEntity != null) {
            mapDbToProvider(dbEntity)
        } else {
            null
        }
    }

}