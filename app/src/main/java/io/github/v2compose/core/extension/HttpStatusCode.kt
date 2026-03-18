package io.github.v2compose.core.extension

import java.net.HttpURLConnection

val Int.isRedirect: Boolean
    get() = when (this) {
        307, // HTTP_TEMP_REDIRECT
        308, // HTTP_PERM_REDIRECT
        HttpURLConnection.HTTP_MULT_CHOICE,
        HttpURLConnection.HTTP_MOVED_PERM,
        HttpURLConnection.HTTP_MOVED_TEMP,
        HttpURLConnection.HTTP_SEE_OTHER -> true

        else -> false
    }
