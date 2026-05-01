package noorg.kloud.vthelper.ui.components.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import noorg.kloud.vthelper.setAlpha

@Composable
fun HorizontalLoadingDivider(
    isLoading: Boolean,
    color: Color,
    padding: PaddingValues.Absolute
) {

    val progressPadding = remember {
        PaddingValues.Absolute(
            top = padding.calculateTopPadding() - 1.5.dp,
            bottom = padding.calculateBottomPadding() - 1.5.dp,
            left = padding.calculateLeftPadding(LayoutDirection.Ltr),
            right = padding.calculateRightPadding(LayoutDirection.Ltr)
        )
    }

    if (isLoading) {
        LinearProgressIndicator(
            modifier = Modifier
                .padding(progressPadding)
                .fillMaxWidth(),
            color = color
        )
    } else {
        HorizontalDivider(
            color = color,
            modifier = Modifier.padding(padding)
        )
    }
}