package noorg.kloud.vthelper.data.data_providers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.data.dbdaos.mano.ManoEmployeeDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSemesterDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSubjectDao
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSemesterEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEntityWithEmployee
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEvaluationVerdict
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSemesterEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSubjectEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSubjectEvaluationVerdict
import noorg.kloud.vthelper.fuzzyFindEmployeeNullIfDash

class ManoSemesterAndSubjectProvider(
    private val manoSemesterDao: ManoSemesterDao,
    private val manoEmployeeDao: ManoEmployeeDao,
    private val manoSubjectDao: ManoSubjectDao,
    private val manoEmployeeProvider: ManoEmployeeProvider
) {

    private val employeeList = mutableListOf<DBManoEmployeeEntity>()

    private suspend fun fetchEmployeesIfNeeded(): Result<String> {
        if (employeeList.isNotEmpty()) {
            return Result.success("OK")
        }
        val lecturersFetchResult = manoEmployeeProvider.fetchEmployeeListFromApi()
        if (lecturersFetchResult.isFailure) {
            return lecturersFetchResult
        }

        val employees = manoEmployeeDao.getAll()
        employeeList.clear()
        employeeList.addAll(employees)

        return Result.success("OK")
    }

    suspend fun fetchAllSemestersAndSubjectsFromApi(): Result<String> {

        fetchCurrentSemesterAndSubjectsFromApi().apply { if (isFailure) return this }
        fetchCompletedSemestersWithResultsFromApi().apply { if (isFailure) return this }

        // TODO: Re-fetch when current semester changes/once every 3 months

        return Result.success("OK")
    }

    suspend fun fetchCurrentSemesterAndSubjectsFromApi(): Result<String> {

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

        val employeesFetchResult = fetchEmployeesIfNeeded()
        if (employeesFetchResult.isFailure) {
            return employeesFetchResult
        }

        val subjectsToAdd = mutableListOf<DBManoSubjectEntity>()
        for (subject in currentSemesterInfo.subjects) {

            val subjectLecturer = employeeList.fuzzyFindEmployeeNullIfDash(subject.lecturerFullName)

            subjectsToAdd.add(
                DBManoSubjectEntity(
                    semesterSequence = currentSemesterInfo.absoluteSequenceNum,
                    lecturerId = subjectLecturer?.manoId ?: 0,
                    modId = subject.modId,
                    modCode = subject.modCode,
                    link = subject.link,
                    name = subject.name,
                    credits = subject.credits
                )
            )

        }

        manoSubjectDao.upsertMany(subjectsToAdd)

        return Result.success("OK")
    }

    suspend fun fetchCompletedSemestersWithResultsFromApi(): Result<String> {
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

        val employeesFetchResult = fetchEmployeesIfNeeded()
        if (employeesFetchResult.isFailure) {
            return employeesFetchResult
        }

        val subjectsToAdd = mutableListOf<DBManoSubjectEntity>()
        for (semesterResponse in completedSemestersResponse.bodyTyped) {
            for (subject in semesterResponse.finalResults) {

                val subjectLecturer = employeeList.fuzzyFindEmployeeNullIfDash(subject.lecturerShortName)

                subjectsToAdd.add(
                    DBManoSubjectEntity(
                        semesterSequence = semesterResponse.absoluteSequenceNum,
                        lecturerId = subjectLecturer?.manoId ?: 0,
                        modId = subject.modId,
                        modCode = subject.modCode,
                        link = subject.link,
                        name = subject.name,
                        finalCompletionDate = subject.completionDate,
                        finalCompletionGrade = subject.grade,
                        finalEvaluationVerdict = DBManoSubjectEvaluationVerdict.valueOf(subject.evaluationVerdict.name),
                        credits = subject.credits,
                        hours = subject.hours,
                    )
                )
            }
        }

        manoSubjectDao.upsertMany(subjectsToAdd)

        return Result.success("OK")
    }

    // ================ Semesters

    fun mapEmployee(model: DBManoSemesterEntity): ProvidedManoSemesterEntity {
        println("Mapped mano course: ${model.absoluteSequenceNum}")
        with(model) {
            return ProvidedManoSemesterEntity(
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

    fun getAllSemesters(): Flow<List<ProvidedManoSemesterEntity>> {
        return manoSemesterDao
            .getAllAsFlow()
            .map { dbEntities ->
                dbEntities.map { mapEmployee(it) }
            }
    }

    fun getCurrentSemester(): Flow<ProvidedManoSemesterEntity?> {
        return manoSemesterDao
            .getCurrentAsFlow()
            .distinctUntilChanged()
            .map { dbEntities ->
                dbEntities.map { mapEmployee(it) }.firstOrNull()
            }
    }

    // ================ Subjects

    fun mapSubject(model: DBManoSubjectEntityWithEmployee): ProvidedManoSubjectEntity {
        println("Mapped mano subject: ${model.subject.name}")
        with(model.subject) {
            return ProvidedManoSubjectEntity(
                modId = modId,
                modCode = modCode,
                link = link,
                lecturerName = model.employee.shortName,
                lecturerId = lecturerId,
                name = name,
                taGaSplitPercentage = taGaSplitPercentage,
                tries = tries,
                hours = hours,
                credits = credits,
                finalCompletionDate = finalCompletionDate,
                finalCompletionGrade = finalCompletionGrade,
                finalCompletionCumulativeScore = finalCompletionCumulativeScore,
                finalCompletionCreditScore = finalCompletionCreditScore,
                finalEvaluationVerdict =
                    if (finalEvaluationVerdict != null)
                        ProvidedManoSubjectEvaluationVerdict.valueOf(finalEvaluationVerdict.name)
                    else null,
            )
        }
    }

    fun getSubjectsForSemester(semesterAbsoluteSequence: Int): Flow<List<ProvidedManoSubjectEntity>> {
        return manoSubjectDao
            .getForSemesterWithEmployee(semesterAbsoluteSequence)
            .distinctUntilChanged()
            .map { dbEntities ->
                dbEntities.map { mapSubject(it) }
            }
    }


}