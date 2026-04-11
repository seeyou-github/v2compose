package io.github.v2compose

import androidx.compose.runtime.compositionLocalOf

/**
 * Shared platform interaction handlers exposed to common UI.
 */
data class AppPlatformHandlers(
    val openExternalUri: (String) -> Unit,
    val shareContent: (String, String) -> Unit,
    val saveImage: (String) -> Unit,
)

val LocalAppPlatformHandlers = compositionLocalOf<AppPlatformHandlers> {
    error("LocalAppPlatformHandlers not provided")
}
