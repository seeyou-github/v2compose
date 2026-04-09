package io.github.v2compose.core.extension

val Int.isRedirect: Boolean
    get() = when (this) {
        300, // HTTP_MULT_CHOICE
        301, // HTTP_MOVED_PERM
        302, // HTTP_MOVED_TEMP
        303, // HTTP_SEE_OTHER
        307, // HTTP_TEMP_REDIRECT
        308  // HTTP_PERM_REDIRECT
            -> true

        else -> false
    }
