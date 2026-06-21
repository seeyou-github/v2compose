package io.github.v2compose.ui.main.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import io.github.v2compose.core.composableWithAnimation

const val homeTabSettingsRoute = "/home/tab_settings"

fun NavController.navigateToHomeTabSettings() {
    navigate(homeTabSettingsRoute)
}

fun NavGraphBuilder.homeTabSettingsScreen(
    onBackClick: () -> Unit,
) {
    composableWithAnimation(route = homeTabSettingsRoute) {
        HomeTabSettingsScreenRoute(
            onBackClick = onBackClick,
        )
    }
}
