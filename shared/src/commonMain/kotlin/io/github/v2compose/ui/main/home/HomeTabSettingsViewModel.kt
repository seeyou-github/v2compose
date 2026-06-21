package io.github.v2compose.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.shared.bean.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeTabSettingsViewModel(
    private val appPreferences: AppPreferences,
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = appPreferences.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AppSettings.Default,
        )

    fun saveHomeTabs(tabs: List<HomeTabConfig>) {
        viewModelScope.launch {
            val normalized = tabs.normalized()
            appPreferences.homeTabConfigsJson(HomeTabConfig.encodeList(normalized))
        }
    }
}
