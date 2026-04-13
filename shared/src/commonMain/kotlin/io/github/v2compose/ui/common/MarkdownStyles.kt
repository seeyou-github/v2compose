package io.github.v2compose.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownTypography

@Composable
fun compactMarkdownTypography(): MarkdownTypography {
    val typography = MaterialTheme.typography

    return markdownTypography(
        h1 = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        h2 = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        h3 = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        h4 = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        h5 = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        h6 = typography.bodySmall.copy(fontWeight = FontWeight.Medium),
    )
}
