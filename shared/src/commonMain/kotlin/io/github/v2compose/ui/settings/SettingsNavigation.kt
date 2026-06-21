package io.github.v2compose.ui.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import io.github.v2compose.core.composableWithAnimation

const val settingsScreenNavigationRoute = "/settings"
const val appearanceScreenNavigationRoute = "/appearance_settings"

fun NavController.navigateToSettings() {
    navigate(settingsScreenNavigationRoute)
}

fun NavController.navigateToAppearanceSettings() {
    navigate(appearanceScreenNavigationRoute)
}

fun NavGraphBuilder.settingsScreen(
    onBackClick: () -> Unit,
    openUri: (String) -> Unit,
    onLogoutSuccess: () -> Unit,
    onAppearanceSettingsClick: () -> Unit,
) {
    composableWithAnimation(route = settingsScreenNavigationRoute) {
        SettingsScreenRoute(
            onBackClick = onBackClick,
            openUri = openUri,
            onLogoutSuccess = onLogoutSuccess,
            onAppearanceSettingsClick = onAppearanceSettingsClick,
        )
    }
}

fun NavGraphBuilder.appearanceScreen(
    onBackClick: () -> Unit,
) {
    composableWithAnimation(route = appearanceScreenNavigationRoute) {
        AppearanceScreenRoute(
            onBackClick = onBackClick,
        )
    }
}