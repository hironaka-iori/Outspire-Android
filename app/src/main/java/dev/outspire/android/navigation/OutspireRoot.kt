package dev.outspire.android.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.outspire.android.data.repository.OutspireRepository
import dev.outspire.android.designsystem.OutspireBackground
import dev.outspire.android.feature.ViewModelFactory
import dev.outspire.android.feature.account.AccountScreen
import dev.outspire.android.feature.account.AccountViewModel
import dev.outspire.android.feature.academic.AcademicScreen
import dev.outspire.android.feature.academic.AcademicViewModel
import dev.outspire.android.feature.academic.ScoresScreen
import dev.outspire.android.feature.activities.ActivitiesScreen
import dev.outspire.android.feature.activities.ActivitiesViewModel
import dev.outspire.android.feature.explore.ExploreScreen
import dev.outspire.android.feature.explore.PlaceholderScreen
import dev.outspire.android.feature.today.TodayScreen
import dev.outspire.android.feature.today.TodayViewModel

enum class AppTab(val label: String, val icon: ImageVector) {
    TODAY("Today", Icons.Default.Today),
    CLASSES("Class", Icons.Default.CalendarMonth),
    ACTIVITIES("Activities", Icons.Default.Checklist),
    EXPLORE("Explore", Icons.Default.GridView),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutspireRoot(
    repository: OutspireRepository,
    modifier: Modifier = Modifier,
) {
    val accountViewModel: AccountViewModel = viewModel(
        factory = ViewModelFactory { AccountViewModel(repository) },
    )
    val todayViewModel: TodayViewModel = viewModel(
        factory = ViewModelFactory { TodayViewModel(repository) },
    )
    val academicViewModel: AcademicViewModel = viewModel(
        factory = ViewModelFactory { AcademicViewModel(repository) },
    )
    val activitiesViewModel: ActivitiesViewModel = viewModel(
        factory = ViewModelFactory { ActivitiesViewModel(repository) },
    )

    val user by accountViewModel.session.collectAsStateWithLifecycle()
    val accountState by accountViewModel.state.collectAsStateWithLifecycle()
    val todayState by todayViewModel.state.collectAsStateWithLifecycle()
    val academicState by academicViewModel.state.collectAsStateWithLifecycle()
    val activitiesState by activitiesViewModel.state.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(AppTab.TODAY) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(user?.code, user?.isDemo) {
        if (user != null) {
            todayViewModel.load()
            academicViewModel.load()
            activitiesViewModel.load()
        }
    }

    BackHandler(enabled = detail != null) { detail = null }

    OutspireBackground(modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (detail != null) {
                    TopAppBar(
                        title = { Text(detailTitle(detail.orEmpty())) },
                        navigationIcon = {
                            IconButton(onClick = { detail = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                    )
                }
            },
            bottomBar = {
                if (detail == null) {
                    NavigationBar(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)) {
                        AppTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = tab == selectedTab,
                                onClick = { selectedTab = tab },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            val contentModifier = Modifier.padding(innerPadding)
            when (val route = detail) {
                "account" -> AccountScreen(
                    state = accountState,
                    user = user,
                    onCodeChange = accountViewModel::setCode,
                    onPasswordChange = accountViewModel::setPassword,
                    onLogin = { accountViewModel.login { detail = null } },
                    onDemo = { accountViewModel.enterDemo { detail = null } },
                    onLogout = { accountViewModel.logout {} },
                    modifier = contentModifier,
                )
                "scores" -> ScoresScreen(
                    state = academicState,
                    user = user,
                    onSignIn = { detail = "account" },
                    modifier = contentModifier,
                )
                null -> when (selectedTab) {
                    AppTab.TODAY -> TodayScreen(
                        state = todayState,
                        user = user,
                        onSignIn = { detail = "account" },
                        onRefresh = {
                            todayViewModel.load(forceRefresh = true)
                            academicViewModel.load(forceRefresh = true)
                        },
                        onClasses = { selectedTab = AppTab.CLASSES },
                        onActivities = { selectedTab = AppTab.ACTIVITIES },
                        onGrades = { detail = "scores" },
                        modifier = contentModifier,
                    )
                    AppTab.CLASSES -> AcademicScreen(
                        state = academicState,
                        user = user,
                        onSignIn = { detail = "account" },
                        onRefresh = { academicViewModel.load(forceRefresh = true) },
                        onSelectDay = academicViewModel::selectDay,
                        modifier = contentModifier,
                    )
                    AppTab.ACTIVITIES -> ActivitiesScreen(
                        state = activitiesState,
                        user = user,
                        onSignIn = { detail = "account" },
                        modifier = contentModifier,
                    )
                    AppTab.EXPLORE -> ExploreScreen(
                        user = user,
                        onToday = { selectedTab = AppTab.TODAY },
                        onClasses = { selectedTab = AppTab.CLASSES },
                        onActivities = { selectedTab = AppTab.ACTIVITIES },
                        onGrades = { detail = "scores" },
                        onAccount = { detail = "account" },
                        onPlaceholder = { detail = "placeholder:$it" },
                        modifier = contentModifier,
                    )
                }
                else -> PlaceholderScreen(
                    title = route.removePrefix("placeholder:"),
                    modifier = contentModifier,
                )
            }
        }
    }
}

private fun detailTitle(route: String): String = when (route) {
    "account" -> "Account"
    "scores" -> "Grades"
    else -> route.removePrefix("placeholder:")
}
