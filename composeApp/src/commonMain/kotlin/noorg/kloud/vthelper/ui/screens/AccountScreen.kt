package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.AbstractCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.LocalDb
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.data.data_providers.LoggedInUserProvider
import noorg.kloud.vthelper.ui.components.ExpandableCard
import noorg.kloud.vthelper.ui.components.InfoField
import noorg.kloud.vthelper.ui.components.PasswordTextField
import noorg.kloud.vthelper.ui.theme.customColors
import noorg.kloud.vthelper.ui.view_models.LoggedInUserViewModel
import org.jetbrains.compose.resources.painterResource
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.account_circle_24px
import vthelper.composeapp.generated.resources.alternate_email_24px
import vthelper.composeapp.generated.resources.id_card_24px
import vthelper.composeapp.generated.resources.moodle
import vthelper.composeapp.generated.resources.school_24px
import vthelper.composeapp.generated.resources.vt_48px

suspend fun tryLoggingIn(
    api: ManoApi,
    username: String,
    password: String,
    mfaCode: String,
    showSnack: (String) -> Unit
) {
    val result = api.loginIfNeeded(username, password, mfaCode)
    showSnack(result.getFullStatus())
}

@Composable
fun AccountScreen(
    gloablCoroutineScope: CoroutineScope,
    showSnack: (String) -> Unit = {}
) {

    val db = LocalDb.current!!
    val loggedInUserViewModel =
        remember { LoggedInUserViewModel(LoggedInUserProvider(db.loggedInUserDao())) }

    val userState by loggedInUserViewModel.userState.collectAsStateWithLifecycle()
    val mfaCode by loggedInUserViewModel.mfaCode.collectAsStateWithLifecycle()

    val mfaInvalid = mfaCode.length != 6
    val userLoginInvalid = (userState.studentId?.length ?: 0) < 4
    val passwordInvalid = userState.password?.isEmpty() ?: true

    var api = remember { ManoApi() }

    val uriHandler = LocalUriHandler.current

    val loggedInCardHeaderText =
        if (userState.isSessionValid) "Logged in" else "Not logged in"
    val loggedInExpandedCardHeaderText =
        if (userState.isSessionValid) loggedInCardHeaderText else "Please enter your credentials"
    val loggedInTopHeaderText =
        if (userState.isSessionValid) "${userState.fullName} (${userState.studentId})" else "Login into your Vilniustech account"

    val loggedInColor = if (userState.isSessionValid)
        MaterialTheme.customColors.goodResult
    else
        MaterialTheme.customColors.badResult

    val platformContext = LocalPlatformContext.current
    val userAvatarLoader by remember {
        lazy {
            ImageRequest.Builder(platformContext)
                .data(userState.avatarPath)
                .crossfade(true)
                .build()
        }
    }

    LaunchedEffect(Unit) {
        loggedInUserViewModel.fetchUserDataFromDb()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState(0))
            .wrapContentSize(Alignment.TopCenter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (userState.isSessionValid && userState.avatarPath?.isNotEmpty() == true) {
            AsyncImage(
                model = userAvatarLoader,
                contentDescription = null,
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .height(102.dp)
                    .fillMaxWidth()
            )
        } else {
            Icon(
                painter = painterResource(Res.drawable.account_circle_24px),
                contentDescription = null,
                modifier = Modifier
                    .height(102.dp)
                    .fillMaxWidth()
            )
        }
        Text(
            text = loggedInTopHeaderText,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.titleLarge
        )
        ExpandableCard(
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
            internalPadding = 8.dp,  // Distance to the card border
            expandedByDefault = !userState.isSessionValid,
            collapsedContent = {
                Text(
                    color = loggedInColor,
                    text = loggedInCardHeaderText
                )
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = loggedInExpandedCardHeaderText)
                OutlinedTextField(
                    value = userState.studentId ?: "",
                    onValueChange = loggedInUserViewModel::updateStudentId,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("12345678") },
                    label = { Text("Enter your student id") },
                    isError = userLoginInvalid,
                )
                PasswordTextField(
                    value = userState.password ?: "",
                    onValueChange = loggedInUserViewModel::updatePassword,
                    "Enter your password",
                    isError = passwordInvalid,
                )
                OutlinedTextField(
                    value = mfaCode,
                    onValueChange = loggedInUserViewModel::updateMfa,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("123 456") },
                    label = { Text("Enter mfa code") },
                    isError = mfaInvalid,
                )
                Button(
                    onClick = {
                        gloablCoroutineScope.launch {
                            tryLoggingIn(
                                api,
                                userState.studentId ?: "",
                                userState.password ?: "",
                                mfaCode,
                                showSnack
                            )
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(4.dp, 10.dp, 4.dp, 4.dp).width(160.dp),
                    enabled = !(userLoginInvalid || passwordInvalid || mfaInvalid),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = if (userState.isSessionValid)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary,
                        contentColor = if (userState.isSessionValid)
                            MaterialTheme.colorScheme.onError
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        modifier = Modifier.padding(end = 12.dp),
                        text = if (userState.isSessionValid) "Logout" else "Login"
                    )
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Column {
            InfoField(
                Res.drawable.account_circle_24px,
                "Full name",
                userState.fullName ?: "-"
            )
            InfoField(
                Res.drawable.alternate_email_24px,
                "Personal email",
                userState.personalEmail ?: "-"
            )
            InfoField(
                Res.drawable.alternate_email_24px,
                "University email",
                userState.universityEmail ?: "-"
            )
            InfoField(
                Res.drawable.id_card_24px,
                "Student id",
                userState.studentId ?: "-"
            )
            InfoField(Res.drawable.school_24px, "Group", "TODO: Extract group")
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