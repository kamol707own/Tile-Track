package com.example.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.AppContainer
import com.example.R
import com.example.domain.DateUtil
import com.example.domain.UserPreferences
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HabitDetailScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.TileTrackMotion
import com.example.ui.theme.TileTrackTheme
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.HabitDetailViewModel
import com.example.ui.viewmodel.InsightsViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.TasksViewModel
import com.example.ui.viewmodel.TileTrackViewModelFactory
import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

@Serializable
data object TasksRoute

@Serializable
data object InsightsRoute

@Serializable
data object SettingsRoute

@Serializable
data object ArchiveRoute

@Serializable
data class HabitDetailRoute(val habitId: String)

private data class TopLevelNavItem<T : Any>(
    val route: T,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

@Composable
fun TileTrackApp(
    container: AppContainer,
    appContext: Context,
    widthSizeClass: WindowWidthSizeClass,
    initialHabitId: String? = null,
    onConsumedInitialHabitId: () -> Unit = {},
) {
    val preferences by container.preferencesRepository.preferencesFlow.collectAsStateWithLifecycle(
        initialValue = UserPreferences(),
    )

    TileTrackTheme(themeMode = preferences.themeMode) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination

        LaunchedEffect(initialHabitId) {
            if (!initialHabitId.isNullOrBlank()) {
                navController.navigate(HabitDetailRoute(initialHabitId)) {
                    launchSingleTop = true
                }
                onConsumedInitialHabitId()
            }
        }

        val topLevelItems = listOf(
            TopLevelNavItem(
                route = DashboardRoute,
                labelRes = R.string.nav_habits,
                selectedIcon = Icons.Rounded.GridView,
                unselectedIcon = Icons.Outlined.GridView,
                testTag = "nav_tab_habits",
            ),
            TopLevelNavItem(
                route = TasksRoute,
                labelRes = R.string.nav_tasks,
                selectedIcon = Icons.Rounded.TaskAlt,
                unselectedIcon = Icons.Outlined.TaskAlt,
                testTag = "nav_tab_tasks",
            ),
            TopLevelNavItem(
                route = InsightsRoute,
                labelRes = R.string.nav_insights,
                selectedIcon = Icons.Rounded.Insights,
                unselectedIcon = Icons.Outlined.Insights,
                testTag = "nav_tab_insights",
            ),
            TopLevelNavItem(
                route = SettingsRoute,
                labelRes = R.string.nav_settings,
                selectedIcon = Icons.Rounded.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                testTag = "nav_tab_settings",
            ),
        )

        val showTopLevelNav = currentDestination?.let { dest ->
            dest.hasRoute<DashboardRoute>() ||
                dest.hasRoute<TasksRoute>() ||
                dest.hasRoute<InsightsRoute>() ||
                dest.hasRoute<SettingsRoute>()
        } ?: true

        val useNavRail = widthSizeClass != WindowWidthSizeClass.Compact

        val onSelectTab: (Any) -> Unit = { targetRoute ->
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showTopLevelNav && !useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = Dp(0f),
                    ) {
                        topLevelItems.forEach { item ->
                            val selected = currentDestination?.hasRoute(item.route::class) == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { onSelectTab(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = stringResource(item.labelRes),
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                modifier = Modifier.testTag(item.testTag),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                if (showTopLevelNav && useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        topLevelItems.forEach { item ->
                            val selected = currentDestination?.hasRoute(item.route::class) == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { onSelectTab(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = stringResource(item.labelRes),
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                modifier = Modifier.testTag(item.testTag),
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = DashboardRoute,
                        enterTransition = { TileTrackMotion.fadeThroughEnter },
                        exitTransition = { TileTrackMotion.fadeThroughExit },
                        popEnterTransition = { TileTrackMotion.fadeThroughEnter },
                        popExitTransition = { TileTrackMotion.fadeThroughExit },
                    ) {
                        composable<DashboardRoute> {
                            val vm: DashboardViewModel = viewModel(
                                factory = TileTrackViewModelFactory(
                                    repository = container.habitRepository,
                                    preferencesRepository = container.preferencesRepository,
                                    appContext = appContext,
                                ),
                            )
                            val state by vm.uiState.collectAsStateWithLifecycle()
                            DashboardScreen(
                                uiState = state,
                                viewModel = vm,
                                widthSizeClass = widthSizeClass,
                                onHabitClick = { habitId ->
                                    navController.navigate(HabitDetailRoute(habitId))
                                },
                            )
                        }

                        composable<TasksRoute> {
                            val vm: TasksViewModel = viewModel(
                                factory = TileTrackViewModelFactory(
                                    repository = container.habitRepository,
                                    preferencesRepository = container.preferencesRepository,
                                    appContext = appContext,
                                ),
                            )
                            val state by vm.uiState.collectAsStateWithLifecycle()
                            TasksScreen(
                                uiState = state,
                                viewModel = vm,
                                widthSizeClass = widthSizeClass,
                            )
                        }

                        composable<InsightsRoute> {
                            val vm: InsightsViewModel = viewModel(
                                factory = TileTrackViewModelFactory(
                                    repository = container.habitRepository,
                                    preferencesRepository = container.preferencesRepository,
                                    appContext = appContext,
                                ),
                            )
                            val state by vm.uiState.collectAsStateWithLifecycle()
                            InsightsScreen(
                                uiState = state,
                                onHabitClick = { habitId ->
                                    navController.navigate(HabitDetailRoute(habitId))
                                },
                            )
                        }

                        composable<SettingsRoute> {
                            val vm: SettingsViewModel = viewModel(
                                factory = TileTrackViewModelFactory(
                                    repository = container.habitRepository,
                                    preferencesRepository = container.preferencesRepository,
                                    appContext = appContext,
                                ),
                            )
                            val state by vm.uiState.collectAsStateWithLifecycle()
                            SettingsScreen(
                                uiState = state,
                                viewModel = vm,
                                onOpenArchive = { navController.navigate(ArchiveRoute) },
                            )
                        }

                        composable<ArchiveRoute>(
                            enterTransition = { TileTrackMotion.sharedAxisXForwardEnter },
                            exitTransition = { TileTrackMotion.sharedAxisXForwardExit },
                            popEnterTransition = { TileTrackMotion.sharedAxisXBackwardEnter },
                            popExitTransition = { TileTrackMotion.sharedAxisXBackwardExit },
                        ) {
                            val vm: SettingsViewModel = viewModel(
                                factory = TileTrackViewModelFactory(
                                    repository = container.habitRepository,
                                    preferencesRepository = container.preferencesRepository,
                                    appContext = appContext,
                                ),
                            )
                            val state by vm.uiState.collectAsStateWithLifecycle()
                            ArchiveScreen(
                                archivedHabits = state.archivedHabits,
                                weekStartMonday = state.preferences.weekStartMonday,
                                todayEpochDay = DateUtil.todayEpochDay(),
                                onNavigateBack = { navController.popBackStack() },
                                onRestore = { item ->
                                    vm.restoreHabit(item.habit.id, item.reminders)
                                },
                                onDeletePermanently = { item ->
                                    vm.deleteArchivedHabit(item.habit.id, item.reminders)
                                },
                                onHabitClick = { habitId ->
                                    navController.navigate(HabitDetailRoute(habitId))
                                },
                            )
                        }

                        composable<HabitDetailRoute>(
                            enterTransition = { TileTrackMotion.sharedAxisXForwardEnter },
                            exitTransition = { TileTrackMotion.sharedAxisXForwardExit },
                            popEnterTransition = { TileTrackMotion.sharedAxisXBackwardEnter },
                            popExitTransition = { TileTrackMotion.sharedAxisXBackwardExit },
                        ) { entry ->
                            val route = entry.toRoute<HabitDetailRoute>()
                            val vm: HabitDetailViewModel = viewModel(
                                factory = TileTrackViewModelFactory(
                                    repository = container.habitRepository,
                                    preferencesRepository = container.preferencesRepository,
                                    appContext = appContext,
                                    habitId = route.habitId,
                                ),
                            )
                            val habit by vm.habitState.collectAsStateWithLifecycle()
                            val prefs by vm.preferences.collectAsStateWithLifecycle()
                            HabitDetailScreen(
                                habitWithLogs = habit,
                                preferences = prefs,
                                viewModel = vm,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
