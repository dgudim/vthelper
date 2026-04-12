package noorg.kloud.vthelper.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.ui.components.SemesterCard
import noorg.kloud.vthelper.ui.components.common.LoadableListSection
import noorg.kloud.vthelper.ui.components.common.ScreenHeaderTextWithLoader
import noorg.kloud.vthelper.ui.view_models.LoggedInUserViewModel
import noorg.kloud.vthelper.ui.view_models.ManoSemesterViewModel

@Composable
fun ResultsScreen(
    loggedInUserViewModel: LoggedInUserViewModel,
    manoSemesterViewModel: ManoSemesterViewModel,
    showSnack: SnackbarFun
) {

    val semesters by manoSemesterViewModel.semesters.collectAsStateWithLifecycle()

    LoadableListSection(
        loggedInUserViewModel = loggedInUserViewModel,
        items = semesters,
        fetchFunction = {
            manoSemesterViewModel.fetchSemestersFromApi(showSnack)
        },
        header = { isLoading ->
            ScreenHeaderTextWithLoader("Results per semester", isLoading)
        }
    ) { semesterData ->
        SemesterCard(semesterData)
    }
}