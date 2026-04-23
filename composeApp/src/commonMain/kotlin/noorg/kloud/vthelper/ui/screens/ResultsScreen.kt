package noorg.kloud.vthelper.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.ui.components.SemesterCard
import noorg.kloud.vthelper.ui.components.common.LoadableListSection
import noorg.kloud.vthelper.ui.components.common.ScreenHeaderTextWithLoader
import noorg.kloud.vthelper.ui.view_models.LoggedInUserViewModel
import noorg.kloud.vthelper.ui.view_models.ManoEmployeeViewModel
import noorg.kloud.vthelper.ui.view_models.ManoSemesterAndSubjectViewModel

@Composable
fun ResultsScreen(
    loggedInUserViewModel: LoggedInUserViewModel,
    manoEmployeeViewModel: ManoEmployeeViewModel,
    manoSemesterAndSubjectViewModel: ManoSemesterAndSubjectViewModel,
    showSnack: SnackbarFun
) {

    val allSemesters by manoSemesterAndSubjectViewModel.semesters.collectAsStateWithLifecycle()
    val currentSemesterData by manoSemesterAndSubjectViewModel.currentSemester.collectAsStateWithLifecycle()

    val selectedEmployee by manoEmployeeViewModel.selectedEmployee.collectAsStateWithLifecycle()

    LoadableListSection(
        loggedInUserViewModel = loggedInUserViewModel,
        items = allSemesters,
        fetchFunction = {
            manoSemesterAndSubjectViewModel.fetchAllSemestersFromApi(showSnack)
        },
        header = { isLoading ->
            ScreenHeaderTextWithLoader("Results per semester", isLoading)
        },
        displayDirectly = true,
    ) { semesterData ->
        currentSemesterData?.let {
            SemesterCard(
                showSnack,
                manoSemesterAndSubjectViewModel,
                it,
                semesterData
            )
        }
    }
}