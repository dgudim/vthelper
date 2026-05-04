package noorg.kloud.vthelper.data.data_providers

import androidx.compose.ui.graphics.Color
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.api.models.toResultFail
import noorg.kloud.vthelper.api.models.toResultOk
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSemesterDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSettlementGradeDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSettlementGroupDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSubjectDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSubjectExamTimetableDao
import noorg.kloud.vthelper.data.dbentities.mano.DBManoBareEmployeeData
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSemesterEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSettlementGroup
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSettlementGroupWithGrades
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectBasicData
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEntityWithEmployee
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEvaluationVerdict
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectMediateData
import noorg.kloud.vthelper.data.dbentities.mano.DbManoSettlementGrade
import noorg.kloud.vthelper.data.dbentities.mano.DbManoSubjectExamTimetableEntity
import noorg.kloud.vthelper.data.dbentities.mano.getSettlementGroupCompositeKey
import noorg.kloud.vthelper.data.dbentities.mano.getSubjectCompositeKey
import noorg.kloud.vthelper.data.provider_models.ProvidedManoExamTimetableEvent
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSemesterEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSettlementGrade
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSettlementGroup
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSettlementGroupClass
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSubjectEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSubjectEvaluationVerdict
import noorg.kloud.vthelper.fuzzyFindEmployee
import noorg.kloud.vthelper.getHashedColor
import noorg.kloud.vthelper.getSemesterYearRange
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.assignment_24px
import vthelper.composeapp.generated.resources.bar_chart_4_bars_24px
import vthelper.composeapp.generated.resources.docs_24px
import vthelper.composeapp.generated.resources.experiment_24px
import vthelper.composeapp.generated.resources.grading_24px
import kotlin.String
import kotlin.time.Instant

class ManoSemesterAndSubjectProvider(
    private val manoSemesterDao: ManoSemesterDao,
    private val manoSubjectDao: ManoSubjectDao,
    private val manoSettlementGradeDao: ManoSettlementGradeDao,
    private val manoSettlementGroupDao: ManoSettlementGroupDao,
    private val manoSubjectExamTimetableDao: ManoSubjectExamTimetableDao,
    private val manoEmployeeProvider: ManoEmployeeProvider
) {

    private val employeeList = mutableListOf<DBManoBareEmployeeData>()

    private val currentSemesterNumber: AtomicRef<DBManoSemesterEntity?> = atomic(null)

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

    private suspend fun getCurrentSemesterDataCached(): Result<DBManoSemesterEntity> {

        if (currentSemesterNumber.value == null) {
            val current = manoSemesterDao.getCurrent()
            if (current.isEmpty()) {
                return "Current semester data is missing".toResultFail()
            }
            currentSemesterNumber.value = current.first()
        }

        return currentSemesterNumber.value!!.toResultOk()
    }

    suspend fun fetchAllSemestersAndSubjectsFromApiIfNeeded(includeSubjects: Boolean): Result<String> {

        fetchCurrentSemesterAndSubjectsFromApi(includeSubjects).onFailure { return it.toResultFail() }
        fetchCompletedSemestersWithResultsFromApi(includeSubjects).onFailure { return it.toResultFail() }

        return "OK".toResultOk()
    }

    suspend fun fetchCurrentSemesterAndSubjectsFromApi(includeSubjects: Boolean): Result<String> {

        val currentSemesterResponse =
            ManoApi.getThisSemesterInfo(::fetchCurrentSemesterAndSubjectsFromApi.name)
                .onFailure { return toResultFail() }

        val currentSemesterInfo = currentSemesterResponse.bodyTyped!!

        with(currentSemesterInfo) {
            manoSemesterDao.upsert(
                DBManoSemesterEntity(
                    absoluteSequenceNum = absoluteSequenceNum,
                    group = group,
                    studyProgram = studyProgram
                )
            )
        }

        if (!includeSubjects) {
            return "OK".toResultOk()
        }

        fetchEmployeesIfNeeded().onFailure { return it.toResultFail() }

        val mediateDatas =
            fetchMediateResultsForSemester(currentSemesterInfo.absoluteSequenceNum)
                .onFailure { return it.toResultFail() }
                .getOrNull()!!

        val subjectsToAdd = mutableListOf<DBManoSubjectBasicData>()
        for (subject in currentSemesterInfo.subjects) {

            val subjectLecturer = employeeList.fuzzyFindEmployee(subject.lecturerFullName)

            val pk = getSubjectCompositeKey(
                currentSemesterInfo.absoluteSequenceNum,
                subject.modId
            )

            subjectsToAdd.add(
                DBManoSubjectBasicData(
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

        manoSubjectDao.upsertManyBasic(subjectsToAdd)

        fetchMediateResultsForSemester(currentSemesterInfo.absoluteSequenceNum)
            .onFailure { return it.toResultFail() }

        return "OK".toResultOk()
    }

    private suspend fun fetchCompletedSemestersWithResultsFromApi(includeSubjects: Boolean): Result<String> {

        val existingSemesters = manoSemesterDao
            .getAllAsFlow()
            .take(1)
            .first()
            .associateBy { it.absoluteSequenceNum }

        val currentSemester = existingSemesters.entries.first().value

        val completedSemestersResponse =
            ManoApi.getCompletedSemesterResults(::fetchCompletedSemestersWithResultsFromApi.name)
                .onFailure { return toResultFail() }

        manoSemesterDao.upsertMany(
            completedSemestersResponse.bodyTyped!!.map {
                val existing = existingSemesters[it.absoluteSequenceNum]

                if (existing != null) {
                    return@map existing.copy(
                        group = it.group,
                        finalTotalCredits = it.finalTotalCredits,
                        finalWeightedGrade = it.finalWeightedGrade
                    )
                }

                DBManoSemesterEntity(
                    absoluteSequenceNum = it.absoluteSequenceNum,
                    group = it.group,
                    finalTotalCredits = it.finalTotalCredits,
                    finalWeightedGrade = it.finalWeightedGrade
                )
            }.toList()
        )

        if (!includeSubjects) {
            return "OK".toResultOk()
        }

        fetchEmployeesIfNeeded().onFailure { return it.toResultFail() }

        val subjectsToAdd = mutableListOf<DBManoSubjectEntity>()
        for (semesterResponse in completedSemestersResponse.bodyTyped) {

            if (!(semesterResponse.absoluteSequenceNum == currentSemester.absoluteSequenceNum
                        || !existingSemesters.contains(semesterResponse.absoluteSequenceNum))
            ) {
                // Only fetch data for last semester (current) and ones not already in the db
                continue
            }

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
        val currentSemesterData =
            getCurrentSemesterDataCached()
                .onFailure { return it.toResultFail() }
                .getOrNull()!!

        val targetSemesterYearRange =
            getSemesterYearRange(
                currentSemesterData.absoluteSequenceNum,
                semesterAbsoluteSequenceNum
            )

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

        val currentSemesterData =
            getCurrentSemesterDataCached()
                .onFailure { return it.toResultFail() }
                .getOrNull()!!

        val targetSemesterYearRange =
            getSemesterYearRange(
                currentSemesterData.absoluteSequenceNum,
                semesterAbsoluteSequenceNum
            )

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

    fun mapSemester(model: DBManoSemesterEntity, isCurrent: Boolean): ProvidedManoSemesterEntity {
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
                dbEntities.withIndex().map { mapSemester(it.value, it.index == 0) }
            }
    }

    fun getCurrentSemester(): Flow<ProvidedManoSemesterEntity?> {
        return manoSemesterDao
            .getCurrentAsFlow()
            .distinctUntilChanged()
            .map { dbEntities ->
                dbEntities.map { mapSemester(it, true) }.firstOrNull()
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
                color =
                    if (customColor == null)
                        getHashedColor(modCode.hashCode().toLong())
                    else
                        Color(customColor),
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

    fun getAllSubjects(): Flow<List<ProvidedManoSubjectEntity>> {
        return manoSubjectDao
            .getAllWithEmployeeAsFlow()
            .distinctUntilChanged()
            .map { dbEntities ->
                dbEntities.map { mapSubject(it) }
            }
    }

    // ================ Settlements

    fun mapSettlementWithGrades(model: DBManoSettlementGroupWithGrades): ProvidedManoSettlementGroup {
        println("Mapped settlement: [subject mod id: ${model.group.subjectModId}, type: ${model.group.settlementType}, nGrades: ${model.grades.size}]")

        val typeClass = when {
            model.group.settlementType.contains("Homework") -> ProvidedManoSettlementGroupClass.HOMEWORK
            model.group.settlementType.contains("exam") -> ProvidedManoSettlementGroupClass.EXAM
            model.group.settlementType.contains("Laboratory") -> ProvidedManoSettlementGroupClass.LAB
            model.group.settlementType.contains("essay") -> ProvidedManoSettlementGroupClass.ESSAY
            else -> ProvidedManoSettlementGroupClass.OTHER
        }

        return ProvidedManoSettlementGroup(
            settlementType = model.group.settlementType,
            settlementTypeClass = typeClass,
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

    // ================ Exams

    suspend fun fetchExamTimetable(): Result<String> {
        val currentSemesterData =
            getCurrentSemesterDataCached()
                .onFailure { return it.toResultFail() }
                .getOrNull()!!

        val examTimetableFromApi = ManoApi.getExamTimetable(
            ::fetchExamTimetable.name,
            currentSemesterData.group,
            currentSemesterData.absoluteSequenceNum
        ).onFailure { return toResultFail() }

        manoSubjectExamTimetableDao.replaceAll(
            examTimetableFromApi.bodyTyped!!.map {
                DbManoSubjectExamTimetableEntity(
                    subjectName = it.subjectName,
                    subjectModCode = it.subjectModCode,
                    examType = it.examType,
                    examClassroom = it.examClassroom,
                    examCredits = it.examCredits,
                    examDateTimeMsUTC = it.examDateTime.toEpochMilliseconds(),
                    examLecturerFullName = it.examLecturerFullName,
                    consultationDateTimeMsUTC = it.consultationDateTime?.toEpochMilliseconds(),
                    consultationClassroom = it.consultationClassroom
                )
            }
        )

        return "OK".toResultOk()
    }

    fun getExamTimetableEvents(): Flow<List<ProvidedManoExamTimetableEvent>> {
        return manoSubjectExamTimetableDao
            .getAllAsFlow()
            .distinctUntilChanged()
            .map { entities ->
                entities.map {
                    ProvidedManoExamTimetableEvent(
                        subjectName = it.subjectName,
                        subjectModCode = it.subjectModCode,
                        color = getHashedColor(it.subjectModCode.hashCode().toLong()),
                        examType = it.examType,
                        examClassroom = it.examClassroom,
                        examCredits = it.examCredits,
                        examDateTime = Instant.fromEpochMilliseconds(it.examDateTimeMsUTC),
                        examLecturerFullName = it.examLecturerFullName,
                        consultationDateTime = it.consultationDateTimeMsUTC?.let { ts ->
                            Instant.fromEpochMilliseconds(ts)
                        },
                        consultationClassroom = it.consultationClassroom
                    )
                }
            }
    }

}