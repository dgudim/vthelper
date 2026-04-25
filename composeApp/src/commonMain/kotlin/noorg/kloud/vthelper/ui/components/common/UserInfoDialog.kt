package noorg.kloud.vthelper.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import noorg.kloud.vthelper.data.provider_models.ProvidedManoEmployeeEntity
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.account_circle_24px
import vthelper.composeapp.generated.resources.alternate_email_24px
import vthelper.composeapp.generated.resources.call_24px
import vthelper.composeapp.generated.resources.corporate_fare_24px
import vthelper.composeapp.generated.resources.person_24px
import vthelper.composeapp.generated.resources.school_24px

@Composable
fun UserInfoDialog(
    userInfo: ProvidedManoEmployeeEntity,
    isLoading: Boolean,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            Modifier
                .clip(AlertDialogDefaults.shape)
                .fillMaxWidth()
                .background(AlertDialogDefaults.containerColor)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImageWithPlaceholder(
                    userInfo.avatarPath,
                    Res.drawable.account_circle_24px,
                    true,
                    102.dp
                )
                Row {
                    Text(
                        text = userInfo.shortName,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    InfoField(
                        Res.drawable.person_24px,
                        "Full name", userInfo.fullName
                    )
                    InfoField(
                        Res.drawable.school_24px,
                        "Position(s)", userInfo.positions?.replace(", ", "\n")
                    )
                    InfoField(
                        Res.drawable.alternate_email_24px,
                        "Email(s)", userInfo.emails?.replace(", ", "\n")
                    )
                    InfoField(
                        Res.drawable.call_24px,
                        "Phone(s)", userInfo.phones?.replace(", ", "\n")
                    )
                    InfoField(
                        Res.drawable.corporate_fare_24px,
                        "Department(s)", userInfo.departments?.replace(", ", "\n")
                    )
                    InfoField(
                        Res.drawable.corporate_fare_24px,
                        "Office(s)", userInfo.offices?.replace(", ", "\n")
                    )
                }
            }
        }
    }
}