package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.ui.components.SubjectResultEntry
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Composable
fun ResultsScreen(showSnack: SnackbarFun) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Your results",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))

        SubjectResultEntry(
            "DataCenters",
            Color(0xFFA57DFF),
            "Hw 1",
            Clock.System.now(),
            9
        )

        SubjectResultEntry(
            "Fundamentals of data mining",
            Color(0xff7dc9ff),
            "Lab 2",
            Clock.System.now() - 1.days,
            10
        )

        SubjectResultEntry(
            "Fundamentals of data mining",
            Color(0xff7dc9ff),
            "Lab 1",
            Clock.System.now() - 2.days,
            10
        )

    }
}