package noorg.kloud.vthelper.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Month
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSemesterEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSubjectEntity
import noorg.kloud.vthelper.getColorFromGrade
import noorg.kloud.vthelper.setAlpha
import noorg.kloud.vthelper.ui.components.common.ExpandableCard
import noorg.kloud.vthelper.ui.components.common.LoadableListSection
import noorg.kloud.vthelper.ui.theme.customColors
import noorg.kloud.vthelper.ui.view_models.ManoSemesterAndSubjectViewModel

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
fun SubjectCard(subjectData: ProvidedManoSubjectEntity) {
    ExpandableCard(
        border = BorderStroke(0.dp, color = Color.Transparent),
        modifier = Modifier.fillMaxWidth(),
        internalPadding = 8.dp,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        collapsedContent = {
            Column(modifier = Modifier.weight(1F)) {
                Text(
                    fontWeight = FontWeight.Bold,
                    text = subjectData.name
                )
                Text(
                    color = MaterialTheme.colorScheme.outline,
                    text = subjectData.lecturerName
                )
                Text(
                    color = MaterialTheme.colorScheme.outline,
                    text = subjectData.modCode
                )
            }
            if (subjectData.finalCompletionGrade != null) {
                GradeColumn(subjectData.finalCompletionGrade.toFloat())
            }
        },
        expandedContent = {

        }
    )
}

@Composable
fun SemesterCard(
    manoSemesterAndSubjectViewModel: ManoSemesterAndSubjectViewModel,
    semesterData: ProvidedManoSemesterEntity
) {

    val borderColor =
        if (semesterData.isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.setAlpha(
            0.5F
        )

    val subjectsForSemester = remember {
        manoSemesterAndSubjectViewModel.getSubjectsForSemesterAsStateFlow(semesterData.absoluteSequenceNum)
    }

    ExpandableCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        modifier = Modifier.padding(top = 8.dp),
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
                    topText = "${semesterData.yearRange}, ${semesterData.season}",
                    bottomText = semesterData.group,
                    finalGrade = semesterData.finalWeightedGrade,
                    leftSectionText = "${semesterData.absoluteSequenceNum}"
                )
            }
        }, expandedContent = {
            // Collect only when rendered
            val subjectsForSemesterCollected by subjectsForSemester.collectAsStateWithLifecycle()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                HorizontalDivider()

                for ((index, subject) in subjectsForSemesterCollected.withIndex()) {
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                            color = DividerDefaults.color.setAlpha(0.5F)
                        )
                    }
                    SubjectCard(subject)
                }
            }
        })

}
