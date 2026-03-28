package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Month
import noorg.kloud.vthelper.ui.components.InfoField
import org.jetbrains.compose.resources.painterResource
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.account_circle_24px
import vthelper.composeapp.generated.resources.alternate_email_24px
import vthelper.composeapp.generated.resources.id_card_24px
import vthelper.composeapp.generated.resources.link_24px
import vthelper.composeapp.generated.resources.moodle
import vthelper.composeapp.generated.resources.person_24px
import vthelper.composeapp.generated.resources.school_24px
import vthelper.composeapp.generated.resources.vt_48px

@Composable
@Preview
fun AccountScreen(showSnack: (String) -> Unit = {}) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mfa by remember { mutableStateOf("") }

    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState(0))
            .wrapContentSize(Alignment.TopCenter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(Res.drawable.account_circle_24px),
            contentDescription = null,
            modifier = Modifier
                .height(102.dp)
                .fillMaxWidth()
        )
        Text(
            text = "Account information",
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.titleLarge
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Distance to the screen border
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), // Distance to the card border
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("You are not logged in, please log in")
                OutlinedTextField(
                    value = username,
                    onValueChange = { newText -> username = newText },
                    label = { Text("Enter your student id") }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { newText -> password = newText },
                    label = { Text("Enter your password") }
                )
                OutlinedTextField(
                    value = mfa,
                    onValueChange = { newText -> mfa = newText },
                    label = { Text("Enter mfa code") }
                )
                Button(
                    onClick = {},
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(4.dp, 10.dp, 4.dp, 4.dp).width(160.dp)
                ) {
                    Text("Login")
                }
            }
        }
        Column {
            InfoField(Res.drawable.account_circle_24px, "Full name", "unknown")
            InfoField(Res.drawable.alternate_email_24px, "Email", "unknown")
            InfoField(Res.drawable.id_card_24px, "Student id", "unknown")
            InfoField(Res.drawable.school_24px, "Group", "unknown")
            HorizontalDivider(modifier = Modifier.padding(16.dp))
            InfoField(Res.drawable.vt_48px, "Open mano", "mano.vilniustech.lt", {
                uriHandler.openUri("https://mano.vilniustech.lt")
            }, true)
            InfoField(Res.drawable.moodle, "Open moodle", "moodle.vilniustech.lt", {
                uriHandler.openUri("https://moodle.vilniustech.lt")
            }, true)
        }

    }
}