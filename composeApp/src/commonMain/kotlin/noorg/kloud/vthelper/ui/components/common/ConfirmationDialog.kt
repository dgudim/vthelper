package noorg.kloud.vthelper.ui.components.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    icon: DrawableResource,
    dialogOpenState: MutableState<Boolean>,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(painterResource(icon), contentDescription = null)
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = text)
        },
        onDismissRequest = {
            dialogOpenState.value = false
        },
        confirmButton = {
            TextButton(
                onClick = {
                    dialogOpenState.value = false
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    dialogOpenState.value = false
                }
            ) {
                Text("Cancel")
            }
        }
    )
}