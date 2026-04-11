package io.github.v2compose.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType

actual fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
): Modifier = this
