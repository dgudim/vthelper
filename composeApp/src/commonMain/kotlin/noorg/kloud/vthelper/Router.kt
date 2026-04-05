@file:OptIn(ExperimentalMaterial3Api::class)

package noorg.kloud.vthelper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.ui.screens.AccountScreen
import noorg.kloud.vthelper.ui.screens.CalendarScreen
import noorg.kloud.vthelper.ui.screens.CoursesScreen
import noorg.kloud.vthelper.ui.screens.DashboardScreen
import noorg.kloud.vthelper.ui.screens.ResultsScreen
import noorg.kloud.vthelper.ui.screens.SettingsScreen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.account_circle_24px
import vthelper.composeapp.generated.resources.bar_chart_4_bars_24px
import vthelper.composeapp.generated.resources.book_24px
import vthelper.composeapp.generated.resources.calendar_month_24px
import vthelper.composeapp.generated.resources.dashboard_24px
import vthelper.composeapp.generated.resources.menu_24px
import vthelper.composeapp.generated.resources.settings_24px
import vthelper.composeapp.generated.resources.vt_48px

// https://stackoverflow.com/questions/72921484/navigating-between-composables-using-a-navigation-drawer-in-jetpack-compose
// https://developer.android.com/develop/ui/compose/components/drawer
// https://fonts.google.com/icons
@Stable
sealed class NavDrawerItem(var route: String, var icon: DrawableResource, var title: String) {
    object Account : NavDrawerItem("account", Res.drawable.account_circle_24px, "Account")
    object Settings : NavDrawerItem("settings", Res.drawable.settings_24px, "Settings")
    object Dashboard : NavDrawerItem("dashboard", Res.drawable.dashboard_24px, "Dashboard")
    object Calendar : NavDrawerItem("calendar", Res.drawable.calendar_month_24px, "Calendar")
    object Courses : NavDrawerItem("courses", Res.drawable.book_24px, "Courses")
    object Results : NavDrawerItem("results", Res.drawable.bar_chart_4_bars_24px, "Results")
}

@Composable
fun TopBar(scope: CoroutineScope, drawerState: DrawerState) {
    TopAppBar(
        title = { Text("VTHelper") },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    if (drawerState.isClosed) {
                        drawerState.open()
                    } else {
                        drawerState.close()
                    }
                }
            }) {
                Icon(
                    painter = painterResource(Res.drawable.menu_24px),
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
fun DrawerItem(item: NavDrawerItem, selected: Boolean, onItemClick: (NavDrawerItem) -> Unit) {
    NavigationDrawerItem(
        label = { Text(text = item.title) },
        selected = selected,
        icon = {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null
            )
        },
        modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 2.dp),
        onClick = { onItemClick(item) }
    )
}

@Composable
fun Navigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    gloablCoroutineScope: CoroutineScope,
    showSnack: (String) -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = NavDrawerItem.Dashboard.route,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        composable(NavDrawerItem.Dashboard.route) {
            DashboardScreen(showSnack)
        }
        composable(NavDrawerItem.Account.route) {
            AccountScreen(gloablCoroutineScope, showSnack)
        }
        composable(NavDrawerItem.Results.route) {
            ResultsScreen(showSnack)
        }
        composable(NavDrawerItem.Calendar.route) {
            CalendarScreen(showSnack)
        }
        composable(NavDrawerItem.Courses.route) {
            CoursesScreen(showSnack)
        }
        composable(NavDrawerItem.Settings.route) {
            SettingsScreen(showSnack)
        }
    }
}

@Composable
fun NavigationDrawer(
    scope: CoroutineScope,
    drawerState: DrawerState,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {

    val items = listOf(
        NavDrawerItem.Account,
        NavDrawerItem.Settings,
        NavDrawerItem.Dashboard,
        NavDrawerItem.Calendar,
        NavDrawerItem.Courses,
        NavDrawerItem.Results
    )

    ModalNavigationDrawer(
        gesturesEnabled = true,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // https://stackoverflow.com/questions/78225866/how-to-align-a-specific-element-to-the-center-in-jetpack-compose-row
                    Text(
                        "Main menu",
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterStart),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Icon(
                        painter = painterResource(Res.drawable.vt_48px),
                        modifier = Modifier
                            .padding(0.dp, 4.dp, 8.dp, 4.dp)
                            .align(Alignment.CenterEnd),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp))

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    DrawerItem(item = item, selected = currentRoute == item.route, onItemClick = {
                        try {
                            navController.navigate(item.route) {
                                // https://stackoverflow.com/questions/72913451/how-to-save-and-restore-navigation-state-in-jetpack-compose
                                // Pop up everything from the back stack to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items

                                if (currentRoute != null) {
                                    popUpTo(currentRoute) {
                                        saveState = true
                                        inclusive = true
                                    }
                                }

                                // Avoid multiple copies of the same destination when reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        } catch (_: IllegalArgumentException) {
                            // User clicks on several items or some other shenenigans
                        }

                        scope.launch {
                            drawerState.close()
                        }
                    })
                }
            }
        }
    ) {
        Scaffold(
            // https://developer.android.com/develop/ui/compose/components/app-bars
            topBar = { TopBar(scope, drawerState) },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { innerPadding ->
            Navigation(
                navController, innerPadding, scope,
                showSnack = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Long
                        )
                    }
                })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun Router() {

    // https://stackoverflow.com/questions/65368007/what-does-jetpack-compose-remember-actually-do-how-does-it-work-under-the-hood

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    NavigationDrawer(
        scope = scope,
        drawerState = drawerState,
        navController = navController,
        snackbarHostState = snackbarHostState
    )
}