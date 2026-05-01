package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.ui.components.common.ConfirmationDialog
import noorg.kloud.vthelper.ui.components.common.ExpandableCard
import noorg.kloud.vthelper.ui.components.common.InfoField
import noorg.kloud.vthelper.ui.components.common.LoaderTextButton
import noorg.kloud.vthelper.ui.components.PasswordTextField
import noorg.kloud.vthelper.ui.components.common.AsyncImageWithPlaceholder
import noorg.kloud.vthelper.ui.theme.customColors
import noorg.kloud.vthelper.ui.view_models.LoggedInUserAndInternetViewModel
import noorg.kloud.vthelper.ui.view_models.ManoSemesterAndSubjectViewModel
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.account_circle_24px
import vthelper.composeapp.generated.resources.alternate_email_24px
import vthelper.composeapp.generated.resources.book_24px
import vthelper.composeapp.generated.resources.calendar_month_24px
import vthelper.composeapp.generated.resources.id_card_24px
import vthelper.composeapp.generated.resources.logout_24px
import vthelper.composeapp.generated.resources.moodle
import vthelper.composeapp.generated.resources.person_24px
import vthelper.composeapp.generated.resources.school_24px
import vthelper.composeapp.generated.resources.vt_48px

@Composable
fun AccountScreen(
    loggedInUserAndInternetViewModel: LoggedInUserAndInternetViewModel,
    manoSemesterAndSubjectViewModel: ManoSemesterAndSubjectViewModel,
    showSnack: SnackbarFun
) {
    val userState by loggedInUserAndInternetViewModel.userState.collectAsStateWithLifecycle()
    val currentSemester by manoSemesterAndSubjectViewModel.currentSemester.collectAsStateWithLifecycle()

    val localMfaCode by loggedInUserAndInternetViewModel.mfaCode.collectAsStateWithLifecycle()
    val localStudentId by loggedInUserAndInternetViewModel.studentId.collectAsStateWithLifecycle()
    val localPassword by loggedInUserAndInternetViewModel.password.collectAsStateWithLifecycle()

    val mfaInvalid = localMfaCode.length != 6
    val userLoginInvalid = localStudentId.length < 4
    val passwordInvalid = localPassword.isEmpty()

    val uriHandler = LocalUriHandler.current

    val loggedInCardHeaderText =
        if (userState.isSessionValid) "Logged in" else "Not logged in"
    val loggedInExpandedCardHeaderText =
        if (userState.isSessionValid) "Viewing credentials" else "Please enter your credentials"
    val loggedInTopHeaderText =
        if (userState.isSessionValid) "${userState.fullName} (${userState.studentId})" else "Login into your Vilniustech account"

    var isLoading by remember { mutableStateOf(false) }

    val loggedInColor = if (userState.isSessionValid)
        MaterialTheme.customColors.goodResult
    else
        MaterialTheme.customColors.badResult

    val loginButtonColors =
        ButtonDefaults.buttonColors().copy(
            containerColor =
                if (userState.isSessionValid)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
            contentColor =
                if (userState.isSessionValid)
                    MaterialTheme.colorScheme.onError
                else
                    MaterialTheme.colorScheme.onPrimary
        )

    val logoutDialogShown = remember { mutableStateOf(false) }

    if (logoutDialogShown.value) ConfirmationDialog(
        "Confirm logout",
        "Do you really want to log out?",
        Res.drawable.logout_24px,
        logoutDialogShown
    ) {
        loggedInUserAndInternetViewModel.logout(showSnack)
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImageWithPlaceholder(
            userState.avatarPath,
            Res.drawable.account_circle_24px,
            userState.isSessionValid,
            102.dp
        )
        Text(
            text = loggedInTopHeaderText,
            modifier = Modifier.padding(8.dp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleLarge
        )
        ExpandableCard(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Distance to the screen border
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
            internalPadding = 8.dp,  // Distance to the card border
            shouldBeExpanded = !userState.isSessionValid,
            collapsedContent = {
                Text(
                    color = loggedInColor,
                    text = loggedInCardHeaderText,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1F)
                )
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = loggedInExpandedCardHeaderText)
                OutlinedTextField(
                    value = localStudentId,
                    onValueChange = loggedInUserAndInternetViewModel::updateStudentId,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("12345678") },
                    label = { Text("Enter your student id") },
                    isError = userLoginInvalid,
                    enabled = !userState.isSessionValid
                )
                PasswordTextField(
                    value = localPassword,
                    onValueChange = loggedInUserAndInternetViewModel::updatePassword,
                    "Enter your password",
                    isError = passwordInvalid,
                    enabled = !userState.isSessionValid
                )
                if (!userState.isSessionValid) OutlinedTextField(
                    value = localMfaCode,
                    onValueChange = loggedInUserAndInternetViewModel::updateMfa,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("123 456") },
                    label = { Text("Enter mfa code") },
                    isError = mfaInvalid,
                )
                LoaderTextButton(
                    onClick = {
                        if (isLoading) {
                            return@LoaderTextButton
                        }
                        if (userState.isSessionValid) {
                            logoutDialogShown.value = true
                        } else {
                            isLoading = true
                            loggedInUserAndInternetViewModel.login(
                                localStudentId, localPassword, localMfaCode,
                                showSnack,
                                onSuccess = {
                                    manoSemesterAndSubjectViewModel.fetchCurrentSemesterFromApi(
                                        showSnack
                                    )
                                }
                            ).invokeOnCompletion {
                                isLoading = false
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .padding(4.dp, 10.dp, 4.dp, 4.dp)
                        .width(160.dp),
                    isLoading = isLoading,
                    enabled = !(userLoginInvalid || passwordInvalid || mfaInvalid || isLoading) || userState.isSessionValid,
                    colors = loginButtonColors,
                    text = if (userState.isSessionValid) "Logout" else "Login"
                )
            }
        }
        Column {
            InfoField(
                Res.drawable.person_24px,
                "Full name",
                userState.fullName
            )
            InfoField(
                Res.drawable.alternate_email_24px,
                "Personal email",
                userState.personalEmail
            )
            InfoField(
                Res.drawable.alternate_email_24px,
                "University email",
                userState.universityEmail
            )
            InfoField(
                Res.drawable.id_card_24px,
                "Student id",
                userState.studentId
            )
            InfoField(
                Res.drawable.school_24px,
                "Group",
                currentSemester?.group
            )
            InfoField(
                Res.drawable.book_24px,
                "Study program",
                currentSemester?.studyProgram
            )
            InfoField(
                Res.drawable.calendar_month_24px,
                "Current semester",
                currentSemester?.absoluteSequenceNum?.toString()
            )
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