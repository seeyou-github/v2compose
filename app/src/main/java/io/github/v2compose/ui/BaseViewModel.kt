package io.github.v2compose.ui

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import io.github.v2compose.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BaseViewModel : ViewModel() {

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    suspend fun updateSnackbarMessage(value: String?) {
        _snackbarMessage.emit(value)
    }

}