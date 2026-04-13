package io.github.v2compose.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics

fun Modifier.autofill(contentType: ContentType): Modifier = semantics {
    this.contentType = contentType
}
