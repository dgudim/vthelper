package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import noorg.kloud.vthelper.ui.components.CourseEntry
import org.jetbrains.compose.resources.painterResource
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.dc
import vthelper.composeapp.generated.resources.isp
import vthelper.composeapp.generated.resources.robotic_process_automation

@Composable
fun CoursesScreen(showSnack: (String) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Moodle courses",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )

        CourseEntry("Data Centers (ELF)",
            "Department of Computer and Communication Technologies",
            "",
            Color(0xFFA57DFF),
            painterResource(Res.drawable.dc)
        )

        CourseEntry("Process Automatization (FMF) ",
            "Department of Information Technology",
            "",
            Color(0xffff887d),
            painterResource(Res.drawable.robotic_process_automation)
        )

        CourseEntry("Provision and Management of Internet Services (ELF)",
            "Department of Computer and Communication Technologies",
            "",
            Color(0xffe77dff),
            painterResource(Res.drawable.isp)
        )

    }
}