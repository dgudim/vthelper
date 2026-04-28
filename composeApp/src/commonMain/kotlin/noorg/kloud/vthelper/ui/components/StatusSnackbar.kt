package noorg.kloud.vthelper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import noorg.kloud.vthelper.ui.theme.customColors

enum class SnackBarSeverityLevel {
    SUCCESS, INFO, WARNING, ERROR, NEUTRAL
}

// https://medium.com/@atharvapajgade/custom-snackbar-in-compose-ccff468e9d09

@Composable
fun StatusSnackbar(snackbarData: SnackbarData) {

    val actionLabel = snackbarData.visuals.actionLabel
    val severity =
        if (actionLabel != null) SnackBarSeverityLevel.valueOf(actionLabel) else SnackBarSeverityLevel.NEUTRAL
    val borderColor = when (severity) {
        SnackBarSeverityLevel.INFO -> MaterialTheme.colorScheme.tertiaryContainer
        SnackBarSeverityLevel.SUCCESS -> MaterialTheme.customColors.goodResult
        SnackBarSeverityLevel.ERROR -> MaterialTheme.customColors.badResult
        SnackBarSeverityLevel.NEUTRAL -> MaterialTheme.colorScheme.outline
        SnackBarSeverityLevel.WARNING -> MaterialTheme.customColors.okResult
    }

    val corners = RoundedCornerShape(16)

    Box(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, shape = corners)
            .border(width = 1.dp, color = borderColor, shape = corners),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            snackbarData.visuals.message,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(8.dp)
        )
    }

}