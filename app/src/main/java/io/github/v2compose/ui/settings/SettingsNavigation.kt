package io.github.v2compose.ui.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val settingsScreenNavigationRoute = "/settings"

fun NavController.navigateToSettings() {
    navigate(settingsScreenNavigationRoute)
}

fun NavGraphBuilder.settingsScreen(
    onBackClick: () -> Unit,
    openUri: (String) -> Unit,
    onLogoutSuccess: () -> Unit
) {
    composable(route = settingsScreenNavigationRoute) {
        SettingsScreenRoute(
            onBackClick = onBackClick,
            openUri = openUri,
            onLogoutSuccess = onLogoutSuccess
        )
    }
}