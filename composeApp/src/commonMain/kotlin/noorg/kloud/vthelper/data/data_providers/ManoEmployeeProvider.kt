package noorg.kloud.vthelper.data.data_providers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.data.dbdaos.mano.ManoEmployeeDao
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoEmployeeEntity
import kotlin.Long

class ManoEmployeeProvider(private val manoEmployeeDao: ManoEmployeeDao) {

    suspend fun fetchEmployeeListFromApi(): Result<String> {
        // Insert a placeholder for the cases when the subject lecturer can't be mapped for some reason
        manoEmployeeDao.upsert(
            DBManoEmployeeEntity(
                manoId = 0,
                shortName = "-"
            )
        )

        val employeeResponse = ManoApi.getEmployees()

        if (employeeResponse.isFailure) {
            return employeeResponse.toResult()
        }

        manoEmployeeDao.upsertMany(employeeResponse.bodyTyped!!.map {
            DBManoEmployeeEntity(
                manoId = it.id,
                shortName = it.shortName
            )
        })

        return Result.success("OK")
    }

    suspend fun fetchEmployeeDetailsFromApi(employeeId: Long): Result<String> {
        val employeeDetailsResponse = ManoApi.getEmployeeDetails(employeeId)

        if (employeeDetailsResponse.isFailure) {
            return employeeDetailsResponse.toResult()
        }

        val existingEmployee = manoEmployeeDao.getById(employeeId)
            ?: return Result.failure(
                Exception("Could not find employee with id: $employeeId")
            )

        manoEmployeeDao.upsert(
            with(employeeDetailsResponse.bodyTyped!!) {
                DBManoEmployeeEntity(
                    manoId = employeeId,
                    shortName = existingEmployee.shortName,
                    fullName = fullNameWithPrefix,
                    emails = emails.joinToString(", "),
                    phones = phones.joinToString(", "),
                    positions = positions.joinToString(", "),
                    offices = offices.joinToString(", ") { "${it.officeName} (${it.address})" },
                    departments = departments.joinToString(", ") { it.name },
                )
            }

        )

        return Result.success("OK")
    }

    private fun mapDbToProvider(model: DBManoEmployeeEntity): ProvidedManoEmployeeEntity {
        println("Mapped employee: ${model.shortName}")
        with(model) {
            return ProvidedManoEmployeeEntity(
                manoId = manoId,
                fullName = fullName,
                shortName = shortName,
                positions = positions,
                departments = departments,
                phones = phones,
                emails = emails,
                offices = offices,
            )
        }
    }

    fun getAllEmployees(): Flow<List<ProvidedManoEmployeeEntity>> {
        return manoEmployeeDao
            .getAllAsFlow()
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