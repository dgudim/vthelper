package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.ui.components.SemesterCard
import noorg.kloud.vthelper.ui.components.common.LoadableListSection
import noorg.kloud.vthelper.ui.components.common.ScreenHeaderTextWithLoader
import noorg.kloud.vthelper.ui.components.common.UserInfoDialog
import noorg.kloud.vthelper.ui.view_models.LoggedInUserAndInternetViewModel
import noorg.kloud.vthelper.ui.view_models.ManoEmployeeViewModel
import noorg.kloud.vthelper.ui.view_models.ManoSemesterAndSubjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    loggedInUserAndInternetViewModel: LoggedInUserAndInternetViewModel,
    manoEmployeeViewModel: ManoEmployeeViewModel,
    manoSemesterAndSubjectViewModel: ManoSemesterAndSubjectViewModel,
    showSnack: SnackbarFun
) {

    val allSemesters by manoSemesterAndSubjectViewModel.semesters.collectAsStateWithLifecycle()
    val currentSemesterData by manoSemesterAndSubjectViewModel.currentSemester.collectAsStateWithLifecycle()

    val selectedEmployee by manoEmployeeViewModel.selectedEmployee.collectAsStateWithLifecycle()

    selectedEmployee.data?.let {
        UserInfoDialog(it, selectedEmployee.isLoading) {
            manoEmployeeViewModel.deselectEmployee()
        }
    }

    LoadableListSection(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp),
        loggedInUserAndInternetViewModel = loggedInUserAndInternetViewModel,
        items = allSemesters,
        fetchFunction = {
            manoSemesterAndSubjectViewModel
                .fetchAllSemestersFromApi(showSnack)
                .await()
        },
        header = { isLoading ->
            ScreenHeaderTextWithLoader("Results per semester", isLoading)
        },
        displayDirectly = true,
        scroll = true
    ) { semesterData ->
        currentSemesterData?.let {
            SemesterCard(
                showSnack,
                manoEmployeeViewModel,
                manoSemesterAndSubjectViewModel,
                it,
                semesterData
            )
        }
    }
}