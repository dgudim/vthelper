package noorg.kloud.vthelper.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSemesterEntity
import noorg.kloud.vthelper.getColorFromGrade
import noorg.kloud.vthelper.ui.components.common.ExpandableCard
import noorg.kloud.vthelper.ui.theme.customColors

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
        Column {
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
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1F)
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


    }
}

@Composable
fun SemesterCard(semesterData: ProvidedManoSemesterEntity) {

    val borderColor =
        if (semesterData.isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent

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

        })

}
