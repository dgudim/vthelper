package noorg.kloud.vthelper.data.data_providers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSemesterDao
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSemesterEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSemesterEntity

class ManoSemesterProvider(
    private val manoSemesterDao: ManoSemesterDao
) {

    suspend fun fetchSemestersFromApi(force: Boolean = false): Result<String> {
        val numSavedSemesters = manoSemesterDao.count()
//        if (numSavedSemesters > 0 && !force) {
//            return Result.success("OK")
//        }

        val currentSemesterResponse = ManoApi.getThisSemesterInfo()

        if (currentSemesterResponse.isFailure) {
            return currentSemesterResponse.toResult()
        }

        val currentSemesterInfo = currentSemesterResponse.bodyTyped!!

        manoSemesterDao.upsert(
            with(currentSemesterInfo) {
                DBManoSemesterEntity(
                    absoluteSequenceNum = absoluteSequenceNum,
                    isCurrent = true,
                    group = group,
                    studyProgram = studyProgram
                )
            }

        )

        // TODO: Re-fetch when current semester changes/once every 3 months

        val completedSemestersResponse = ManoApi.getCompletedSemesterResults()

        if (completedSemestersResponse.isFailure) {
            return completedSemestersResponse.toResult()
        }

        manoSemesterDao.upsertMany(
            completedSemestersResponse.bodyTyped!!.map {
                DBManoSemesterEntity(
                    absoluteSequenceNum = it.absoluteSequenceNum,
                    isCurrent = false,
                    group = it.group,
                    season = it.sessionSeason,
                    yearRange = it.yearTimeSpan,
                    finalTotalCredits = it.finalTotalCredits,
                    finalWeightedGrade = it.finalWeightedGrade
                )
            }.toList()
        )

        return Result.success("OK")
    }

    fun getAllSemesters(): Flow<List<ProvidedManoSemesterEntity>> {
        return manoSemesterDao
            .getAllAsFlow()
            .map { dbEntities ->
                dbEntities.map { dbEntity ->
                    println("Mapped mano course: ${dbEntity.absoluteSequenceNum}")
                    with(dbEntity) {
                        ProvidedManoSemesterEntity(
                            absoluteSequenceNum = absoluteSequenceNum,
                            isCurrent = isCurrent,
                            group = group,
                            studyProgram = studyProgram ?: "",
                            season = season ?: "",
                            yearRange = yearRange ?: "",
                            finalTotalCredits = finalTotalCredits,
                            finalWeightedGrade = finalWeightedGrade
                        )
                    }
                }
            }
    }

    fun getCurrentSemester(): Flow<ProvidedManoSemesterEntity?> {
        return getAllSemesters()
            .map { semesters -> semesters.firstOrNull { it.isCurrent } }
    }

}