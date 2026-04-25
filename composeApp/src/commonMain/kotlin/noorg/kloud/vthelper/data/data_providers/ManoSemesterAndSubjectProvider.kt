package noorg.kloud.vthelper.data.data_providers

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.api.models.toResultFail
import noorg.kloud.vthelper.api.models.toResultOk
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSemesterDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSettlementGradeDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSettlementGroupDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSubjectDao
import noorg.kloud.vthelper.data.dbentities.mano.DBManoBareEmployeeData
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSemesterEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSettlementGroup
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSettlementGroupWithGrades
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEntityWithEmployee
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEvaluationVerdict
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectMediateData
import noorg.kloud.vthelper.data.dbentities.mano.DbManoSettlementGrade
import noorg.kloud.vthelper.data.dbentities.mano.getSettlementGroupCompositeKey
import noorg.kloud.vthelper.data.dbentities.mano.getSubjectCompositeKey
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSemesterEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSettlementGrade
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSettlementGroup
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSubjectEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSubjectEvaluationVerdict
import noorg.kloud.vthelper.fuzzyFindEmployee
import noorg.kloud.vthelper.getSemesterYearRange
import kotlin.String
import kotlin.concurrent.Volatile

class ManoSemesterAndSubjectProvider(
    private val manoSemesterDao: ManoSemesterDao,
    private val manoSubjectDao: ManoSubjectDao,
    private val manoSettlementGradeDao: ManoSettlementGradeDao,
    private val manoSettlementGroupDao: ManoSettlementGroupDao,
    private val manoEmployeeProvider: ManoEmployeeProvider
) {

    private val employeeList = mutableListOf<DBManoBareEmployeeData>()

    private val currentSemesterNumber: AtomicInt = atomic(0)

    private suspend fun fetchEmployeesIfNeeded(): Result<String> {
        // It's important to actually put the employees into the DB. Needed for FK constraints
        if (employeeList.isEmpty()) {
            manoEmployeeProvider.fetchEmployeeListFromApi()
                .onFailure { return it.toResultFail() }
                .onSuccess {
                    employeeList.clear()
                    employeeList.addAll(it)
                }
        }

        return "OK".toResultOk()
    }

    private suspend fun fetchCurrentSemesterNumberIfNeeded(): Result<Int> {

        if (currentSemesterNumber.value == 0) {
            ManoApi.getThisSemesterInfo(::fetchCurrentSemesterNumberIfNeeded.name)
                .onFailure { return toResultFail() }
                .onSuccess { currentSemesterNumber.value = it.absoluteSequenceNum }
        }

        return currentSemesterNumber.value.toResultOk()
    }

    suspend fun fetchAllSemestersAndSubjectsFromApi(): Result<String> {

        fetchCurrentSemesterAndSubjectsFromApi().onFailure { return it.toResultFail() }

        if (manoSemesterDao.count() <= 1) {
            fetchCompletedSemestersWithResultsFromApi().onFailure { return it.toResultFail() }
        }

        return "OK".toResultOk()
    }

    suspend fun fetchCurrentSemesterAndSubjectsFromApi(): Result<String> {

        val currentSemesterResponse =
            ManoApi.getThisSemesterInfo(::fetchCurrentSemesterAndSubjectsFromApi.name)
                .onFailure { return toResultFail() }

        val currentSemesterInfo = currentSemesterResponse.bodyTyped!!
        currentSemesterNumber.value = currentSemesterInfo.absoluteSequenceNum

        with(currentSemesterInfo) {
            manoSemesterDao.upsert(
                DBManoSemesterEntity(
                    absoluteSequenceNum = absoluteSequenceNum,
                    isCurrent = true,
                    group = group,
                    studyProgram = studyProgram
                )
            )
        }

        fetchEmployeesIfNeeded().onFailure { return it.toResultFail() }

        val mediateDatas =
            fetchMediateResultsForSemester(currentSemesterInfo.absoluteSequenceNum)
                .onFailure { return it.toResultFail() }
                .getOrNull()!!

        val subjectsToAdd = mutableListOf<DBManoSubjectEntity>()
        for (subject in currentSemesterInfo.subjects) {

            val subjectLecturer = employeeList.fuzzyFindEmployee(subject.lecturerFullName)

            val pk = getSubjectCompositeKey(
                currentSemesterInfo.absoluteSequenceNum,
                subject.modId
            )

            subjectsToAdd.add(
                DBManoSubjectEntity(
                    compositePrimaryId = pk,
                    semesterAbsoluteSeq = currentSemesterInfo.absoluteSequenceNum,
                    lecturerId = subjectLecturer?.manoId ?: 0,
                    modId = subject.modId,
                    modCode = subject.modCode,
                    link = subject.link,
                    name = subject.name,
                    credits = subject.credits,
                    mediateData = mediateDatas[pk] ?: DBManoSubjectMediateData()
                )
            )

        }

        manoSubjectDao.upsertMany(subjectsToAdd)

        fetchMediateResultsForSemester(currentSemesterInfo.absoluteSequenceNum)
            .onFailure { return it.toResultFail() }

        return "OK".toResultOk()
    }

    suspend fun fetchCompletedSemestersWithResultsFromApi(): Result<String> {

        val completedSemestersResponse = ManoApi.getCompletedSemesterResults(::fetchCompletedSemestersWithResultsFromApi.name)
            .onFailure { return toResultFail() }

        manoSemesterDao.upsertMany(
            completedSemestersResponse.bodyTyped!!.map {
                DBManoSemesterEntity(
                    absoluteSequenceNum = it.absoluteSequenceNum,
                    isCurrent = false,
                    group = it.group,
                    finalTotalCredits = it.finalTotalCredits,
                    finalWeightedGrade = it.finalWeightedGrade
                )
            }.toList()
        )

        fetchEmployeesIfNeeded().onFailure { return it.toResultFail() }

        val subjectsToAdd = mutableListOf<DBManoSubjectEntity>()
        for (semesterResponse in completedSemestersResponse.bodyTyped) {

            val mediateDatas =
                fetchMediateResultsForSemester(semesterResponse.absoluteSequenceNum)
                    .onFailure { return it.toResultFail() }
                    .getOrNull()!!

            for (subject in semesterResponse.finalResults) {

                val subjectLecturer =
                    employeeList.fuzzyFindEmployee(subject.lecturerShortName)

                val pk = getSubjectCompositeKey(
                    semesterResponse.absoluteSequenceNum,
                    subject.modId
                )

                subjectsToAdd.add(
                    DBManoSubjectEntity(
                        compositePrimaryId = pk,
                        semesterAbsoluteSeq = semesterResponse.absoluteSequenceNum,
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
                        mediateData = mediateDatas[pk] ?: DBManoSubjectMediateData()
                    )
                )
            }

        }

        manoSubjectDao.upsertMany(subjectsToAdd)

        return "OK".toResultOk()
    }

    suspend fun fetchMediateResultsForSemester(
        semesterAbsoluteSequenceNum: Int
    ): Result<Map<String, DBManoSubjectMediateData>> {
        val currentSemesterSequenceNum =
            fetchCurrentSemesterNumberIfNeeded()
                .onFailure { return it.toResultFail() }
                .getOrNull() ?: 0

        val targetSemesterYearRange =
            getSemesterYearRange(currentSemesterSequenceNum, semesterAbsoluteSequenceNum)

        val mediateResultsResponse =
            ManoApi.getSemesterMediateResults(
                ::fetchMediateResultsForSemester.name,
                semesterAbsoluteSequenceNum,
                targetSemesterYearRange
            ).onFailure { return toResultFail() }

        val mapped = mediateResultsResponse.bodyTyped!!.datas.results.associate {
            getSubjectCompositeKey(
                semesterAbsoluteSequenceNum,
                it.modId.toInt()
            ) to DBManoSubjectMediateData(
                mediateResultsAvailable = true,
                taGaSplitPercentage = it.taGaSplitPercentage,
                finalCompletionCreditScore = it.fullCreditAndScore,
                finalCompletionCumulativeScore = it.cumulativeScore
            )
        }

        return mapped.toResultOk()
    }

    suspend fun fetchSettlementGroupsForSubjectInSemester(
        semesterAbsoluteSequenceNum: Int,
        subjectModId: Int,
    ): Result<String> {

        val currentSemesterSequenceNum =
            fetchCurrentSemesterNumberIfNeeded()
                .onFailure { return it.toResultFail() }
                .getOrNull() ?: 0

        val targetSemesterYearRange =
            getSemesterYearRange(currentSemesterSequenceNum, semesterAbsoluteSequenceNum)

        val settlementGroupsResponse =
            ManoApi.getSubjectSettlementGroups(
                ::fetchSettlementGroupsForSubjectInSemester.name,
                semesterAbsoluteSequenceNum,
                targetSemesterYearRange,
                subjectModId
            ).onFailure { return toResultFail() }

        val settlementGroups = settlementGroupsResponse.bodyTyped!!

        manoSettlementGroupDao.upsertMany(settlementGroups.map {
            DBManoSettlementGroup(
                compositePrimaryId = getSettlementGroupCompositeKey(
                    semesterAbsoluteSequenceNum,
                    subjectModId,
                    it.settlementType
                ),
                compositeSubjectId = getSubjectCompositeKey(
                    semesterAbsoluteSequenceNum,
                    subjectModId
                ),
                settlementType = it.settlementType,
                subjectModId = subjectModId,
                semesterAbsoluteSequenceNum = semesterAbsoluteSequenceNum,
                completedRatio = it.completedRatio,
                percentageOfFinalAssessment = it.percentageOfFinalAssessment,
                finalGrade = it.finalGrade,
                finalCumulativeScore = it.finalCumulativeScore,
                lastUpdatedDate = it.lastUpdatedDate
            )
        })

        fetchEmployeesIfNeeded().onFailure { return it.toResultFail() }

        for (settlementGroup in settlementGroups) {
            val gradesResponse = ManoApi.getSettlementGrades(
                ::fetchSettlementGroupsForSubjectInSemester.name,
                settlementGroup.semesterRelativeSequenceNum,
                subjectModId,
                settlementGroup.subjectKmdId,
                settlementGroup.subjectName,
                settlementGroup.subjectDestVart,
                targetSemesterYearRange,
                settlementGroup.settlementType
            ).onFailure { return toResultFail() }

            manoSettlementGradeDao.upsertMany(
                gradesResponse.bodyTyped!!.map {

                    val grader =
                        employeeList.fuzzyFindEmployee(it.graderName)

                    val baseKey = getSettlementGroupCompositeKey(
                        semesterAbsoluteSequenceNum,
                        subjectModId,
                        settlementGroup.settlementType
                    )

                    DbManoSettlementGrade(
                        compositePrimaryId = "${baseKey}-name_${it.name}",
                        compositeSettlementId = baseKey,
                        name = it.name,
                        value = it.grade,
                        date = it.gradeDate,
                        graderId = grader?.manoId ?: 0
                    )
                }
            )
        }

        return "OK".toResultOk()
    }

    // ================ Semesters

    fun mapSemester(model: DBManoSemesterEntity): ProvidedManoSemesterEntity {
        println("Mapped mano semester: ${model.absoluteSequenceNum}")
        with(model) {
            return ProvidedManoSemesterEntity(
                absoluteSequenceNum = absoluteSequenceNum,
                isCurrent = isCurrent,
                group = group,
                studyProgram = studyProgram ?: "",
                finalTotalCredits = finalTotalCredits,
                finalWeightedGrade = finalWeightedGrade
            )
        }
    }

    fun getAllSemesters(): Flow<List<ProvidedManoSemesterEntity>> {
        return manoSemesterDao
            .getAllAsFlow()
            .distinctUntilChanged()
            .map { dbEntities ->
                dbEntities.map { mapSemester(it) }
            }
    }

    fun getCurrentSemester(): Flow<ProvidedManoSemesterEntity?> {
        return manoSemesterDao
            .getCurrentAsFlow()
            .distinctUntilChanged()
            .map { dbEntities ->
                dbEntities.map { mapSemester(it) }.firstOrNull()
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
                taGaSplitPercentage = mediateData.taGaSplitPercentage,
                mediateResultsAvailable = mediateData.mediateResultsAvailable,
                tries = tries,
                hours = hours,
                credits = credits,
                finalCompletionDate = finalCompletionDate,
                finalCompletionGrade = finalCompletionGrade,
                finalCompletionCumulativeScore = mediateData.finalCompletionCumulativeScore,
                finalCompletionCreditScore = mediateData.finalCompletionCreditScore,
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

    // ================ Settlements

    fun mapSettlementWithGrades(model: DBManoSettlementGroupWithGrades): ProvidedManoSettlementGroup {
        println("Mapped settlement: [subject mod id: ${model.group.subjectModId}, type: ${model.group.settlementType}, nGrades: ${model.grades.size}]")
        return ProvidedManoSettlementGroup(
            settlementType = model.group.settlementType,
            completedRatio = model.group.completedRatio,
            percentageOfFinalAssessment = model.group.percentageOfFinalAssessment,
            finalGrade = model.group.finalGrade,
            finalCumulativeScore = model.group.finalCumulativeScore,
            lastUpdatedDate = model.group.lastUpdatedDate,
            grades = model.grades.map {
                ProvidedManoSettlementGrade(
                    name = it.grade.name,
                    value = it.grade.value,
                    date = it.grade.date,
                    graderShortName = it.employee.shortName,
                    graderId = it.grade.graderId,
                )
            }.toList()
        )
    }

    fun getSettlementGroupsForSubjectInSemester(
        semAbsoluteSequenceNum: Int,
        subjectModId: Int
    ): Flow<List<ProvidedManoSettlementGroup>> {
        return manoSettlementGroupDao
            .getForSubjectInSemesterWithGrades(semAbsoluteSequenceNum, subjectModId)
            .distinctUntilChanged()
            .map { dbEntities ->
                dbEntities.map { mapSettlementWithGrades(it) }
            }
    }


}