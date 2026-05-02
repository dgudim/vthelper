package noorg.kloud.vthelper.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSemesterEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSettlementGroup
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSubjectEntity
import noorg.kloud.vthelper.getColorFromGrade
import noorg.kloud.vthelper.getSemesterSessionSeason
import noorg.kloud.vthelper.getSemesterYearRange
import noorg.kloud.vthelper.mixedWithPrimary
import noorg.kloud.vthelper.setAlpha
import noorg.kloud.vthelper.ui.components.common.ExpandableCard
import noorg.kloud.vthelper.ui.components.common.HorizontalLoadingDivider
import noorg.kloud.vthelper.ui.components.common.SmartFetcher
import noorg.kloud.vthelper.ui.theme.customColors
import noorg.kloud.vthelper.ui.view_models.LoggedInUserAndInternetViewModel
import noorg.kloud.vthelper.ui.view_models.ManoEmployeeViewModel
import noorg.kloud.vthelper.ui.view_models.ManoSemesterViewModel
import org.jetbrains.compose.resources.painterResource
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.person_24px

@Composable
fun GradeColumn(finalGrade: Float) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        Text(
            color = MaterialTheme.colorScheme.outline,
            text = "Grade"
        )
        Text(
            text = "$finalGrade",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.customColors.getColorFromGrade(finalGrade),
        )
    }
}

@Composable
fun RowScope.CollapsedTitle(
    topText: String,
    bottomText: String,
    finalGrade: Float?,
    leftSectionText: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .weight(1F)
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                color = MaterialTheme.colorScheme.outline,
                text = "sem"
            )
            Text(
                text = leftSectionText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Column(modifier = Modifier.weight(1F)) {
            Text(
                fontWeight = FontWeight.Bold,
                text = topText
            )
            Text(
                color = MaterialTheme.colorScheme.outline,
                text = bottomText
            )
        }

        if (finalGrade != null) {
            GradeColumn(finalGrade)
        }
    }
}

@Composable
fun SettlementGroupCard(settlementGroup: ProvidedManoSettlementGroup) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1F)) {
            Row {
                Text(
                    color = MaterialTheme.colorScheme.outline,
                    text = settlementGroup.completedRatio,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.width(50.dp).padding(end = 4.dp)
                )
                Text(
                    fontWeight = FontWeight.Bold,
                    text = settlementGroup.settlementType
                )
            }
            Row {
                Text(
                    text = "Individual: ",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
                for (grade in settlementGroup.grades) {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = "${grade.value ?: "-"}",
                        color =
                            if (grade.value != null)
                                MaterialTheme.customColors.getColorFromGrade(
                                    grade.value.toFloat()
                                )
                            else
                                Color.Unspecified
                    )
                }
            }
        }

        if (settlementGroup.finalGrade != null) {
            GradeColumn(settlementGroup.finalGrade)
        }
    }
}

@Composable
fun SubjectCard(
    showSnack: SnackbarFun,
    manoEmployeeViewModel: ManoEmployeeViewModel,
    manoSemesterViewModel: ManoSemesterViewModel,
    loggedInUserAndInternetViewModel: LoggedInUserAndInternetViewModel,
    semAbsoluteSequenceNum: Int,
    subjectData: ProvidedManoSubjectEntity
) {

    val settlementGroupsWithGrades = remember {
        manoSemesterViewModel.getSettlementGroupsForSubjectAsStateFlow(
            semAbsoluteSequenceNum,
            subjectData.modId
        )
    }

    val isLoadingState = remember { mutableStateOf(false) }
    val isLoading by isLoadingState

    ExpandableCard(
        border = BorderStroke(0.dp, color = Color.Transparent),
        modifier = Modifier.fillMaxWidth(),
        internalPadding = 8.dp,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        expansionAvailable = subjectData.mediateResultsAvailable,
        collapsedContent = {
            Column(modifier = Modifier.weight(1F)) {
                Text(
                    fontWeight = FontWeight.Bold,
                    text = subjectData.name
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        color = MaterialTheme.colorScheme.outline,
                        text = subjectData.lecturerName
                    )
                    if (subjectData.lecturerId > 0) {
                        Icon(
                            painter = painterResource(Res.drawable.person_24px),
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clip(CircleShape)
                                .clickable {
                                    manoEmployeeViewModel.selectEmployeeById(
                                        showSnack,
                                        subjectData.lecturerId
                                    )
                                },
                            tint = MaterialTheme.colorScheme.outline,
                            contentDescription = null
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(end = 4.dp)
                            .clip(CircleShape)
                            .background(subjectData.color.mixedWithPrimary())
                    )
                    Text(
                        color = MaterialTheme.colorScheme.outline,
                        text = subjectData.modCode
                    )
                }

            }
            if (subjectData.finalCompletionGrade != null) {
                GradeColumn(subjectData.finalCompletionGrade.toFloat())
            }
        },
        expandedContent = {
            // Collect only when rendered
            val settlementGroupsWithGradesCollected by settlementGroupsWithGrades.collectAsStateWithLifecycle()

            SmartFetcher(
                loggedInUserAndInternetViewModel,
                isLoadingState
            ) {
                manoSemesterViewModel.fetchSettlementGroupsFromApi(
                    semAbsoluteSequenceNum,
                    subjectData.modId,
                    showSnack
                ).await()
            }

            Row {
                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    HorizontalLoadingDivider(
                        isLoading = isLoading,
                        color = MaterialTheme.colorScheme.tertiary.setAlpha(0.5F),
                        padding = PaddingValues.Absolute(
                            left = 12.dp,
                            top = 10.dp,
                            bottom = 4.dp,
                            right = 12.dp
                        )
                    )
                    for (group in settlementGroupsWithGradesCollected) {
                        SettlementGroupCard(group)
                    }
                }
            }
        }
    )
}

@Composable
fun SemesterCard(
    showSnack: SnackbarFun,
    manoEmployeeViewModel: ManoEmployeeViewModel,
    manoSemesterViewModel: ManoSemesterViewModel,
    loggedInUserAndInternetViewModel: LoggedInUserAndInternetViewModel,
    currentSemesterData: ProvidedManoSemesterEntity,
    semesterData: ProvidedManoSemesterEntity,
) {

    val borderColor =
        if (semesterData.isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.setAlpha(
            0.5F
        )

    val semesterYearRange = remember {
        getSemesterYearRange(
            currentSemesterData.absoluteSequenceNum,
            semesterData.absoluteSequenceNum
        )
    }

    val semesterSessionSeason = remember {
        getSemesterSessionSeason(
            semesterData.absoluteSequenceNum
        )
    }

    val subjectsForSemester = remember {
        manoSemesterViewModel.getSubjectsForSemesterAsStateFlow(semesterData.absoluteSequenceNum)
    }

    ExpandableCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shouldBeExpanded = semesterData.isCurrent,
        internalPadding = 4.dp,
        border = BorderStroke(1.dp, borderColor),
        collapsedContent = {
            if (semesterData.isCurrent) {
                CollapsedTitle(
                    topText = "Current",
                    bottomText = semesterData.group,
                    finalGrade = null,
                    leftSectionText = "${semesterData.absoluteSequenceNum}"
                )
            } else {
                CollapsedTitle(
                    topText = "$semesterYearRange, $semesterSessionSeason",
                    bottomText = semesterData.group,
                    finalGrade = semesterData.finalWeightedGrade,
                    leftSectionText = "${semesterData.absoluteSequenceNum}"
                )
            }
        }, expandedContent = {
            // Collect only when rendered
            val subjectsForSemesterCollected by subjectsForSemester.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                HorizontalDivider()

                for ((index, subject) in subjectsForSemesterCollected.withIndex()) {
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                            color = DividerDefaults.color.setAlpha(0.5F)
                        )
                    }
                    SubjectCard(
                        showSnack,
                        manoEmployeeViewModel,
                        manoSemesterViewModel,
                        loggedInUserAndInternetViewModel,
                        semesterData.absoluteSequenceNum,
                        subject
                    )
                }
            }
        })

}
