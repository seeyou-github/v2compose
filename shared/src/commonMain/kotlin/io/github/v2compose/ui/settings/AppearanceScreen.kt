package io.github.v2compose.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v2compose.shared.bean.AppSettings
import io.github.v2compose.ui.common.BackIcon
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.cancel
import v2compose.shared.generated.resources.ok
import v2compose.shared.generated.resources.settings_appearance
import v2compose.shared.generated.resources.settings_dark_mode
import kotlin.math.roundToInt

// ─── Navigation ──────────────────────────────────────────────────────────────

@Composable
fun AppearanceScreenRoute(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    AppearanceScreen(
        appSettings = appSettings,
        onBackClick = onBackClick,
        onDarkModeChanged = viewModel::setDarkThemeEnabled,
        onTopicListTitleTextSizeChanged = viewModel::setTopicListTitleTextSize,
        onPresetSelected = { index ->
            if (appSettings.darkThemeEnabled) viewModel.setDarkPresetIndex(index)
            else viewModel.setLightPresetIndex(index)
        },
        onColorOverridesChanged = { overrides ->
            if (appSettings.darkThemeEnabled) viewModel.setDarkOverridesJson(overrides)
            else viewModel.setLightOverridesJson(overrides)
        },
    )
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceScreen(
    appSettings: AppSettings,
    onBackClick: () -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onTopicListTitleTextSizeChanged: (Int) -> Unit,
    onPresetSelected: (Int) -> Unit,
    onColorOverridesChanged: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackIcon(onBackClick = onBackClick) },
                title = {
                    Text(
                        stringResource(Res.string.settings_appearance),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Dark mode switch ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDarkModeChanged(!appSettings.darkThemeEnabled) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Res.string.settings_dark_mode),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = appSettings.darkThemeEnabled,
                    onCheckedChange = onDarkModeChanged,
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Theme presets ────────────────────────────────────────────────
            PresetSectionTitle(
                if (appSettings.darkThemeEnabled) "深色主题" else "浅色主题"
            )
            ThemePresetsGrid(
                presets = if (appSettings.darkThemeEnabled) DarkPresets else LightPresets,
                selectedIndex = if (appSettings.darkThemeEnabled) {
                    appSettings.appearanceDarkPresetIndex
                } else {
                    appSettings.appearanceLightPresetIndex
                },
                overridesJson = if (appSettings.darkThemeEnabled) {
                    appSettings.appearanceDarkOverridesJson
                } else {
                    appSettings.appearanceLightOverridesJson
                },
                onPresetSelected = onPresetSelected,
                onColorOverridesChanged = onColorOverridesChanged,
            )

            Spacer(Modifier.height(24.dp))

            // ── Font size sliders ────────────────────────────────────────────
            PresetSectionTitle("文字大小")
            TextSizeSlider(
                label = "帖子标题文字",
                value = appSettings.topicListTitleTextSize.toFloat(),
                range = 5f..25f,
                onValueChange = { onTopicListTitleTextSizeChanged(it.roundToInt()) },
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}

// ─── Section title ───────────────────────────────────────────────────────────

@Composable
private fun PresetSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

// ─── Preset grid ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThemePresetsGrid(
    presets: List<ColorPreset>,
    selectedIndex: Int,
    overridesJson: String,
    onPresetSelected: (Int) -> Unit,
    onColorOverridesChanged: (String) -> Unit,
) {
    var detailPreset by remember { mutableStateOf<ColorPreset?>(null) }
    val currentOverrides = remember(overridesJson) { parseColorOverrides(overridesJson) }

    Column(modifier = Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        presets.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { preset ->
                    val idx = presets.indexOf(preset)
                    val isSelected = idx == selectedIndex
                    PresetCard(
                        preset = preset,
                        isSelected = isSelected,
                        overrides = if (isSelected) currentOverrides else emptyMap(),
                        modifier = Modifier.weight(1f),
                        onSelect = { onPresetSelected(idx) },
                        onShowDetails = { detailPreset = preset },
                    )
                }
                // Fill remaining slots to keep row heights equal
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }

    detailPreset?.let { preset ->
        PresetDetailDialog(
            preset = preset,
            currentOverrides = currentOverrides,
            onDismiss = { detailPreset = null },
            onColorChanged = { key, color ->
                val updated = currentOverrides.toMutableMap().also { it[key] = color }
                onColorOverridesChanged(serializeColorOverrides(updated))
            },
        )
    }
}

// ─── Preset card ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetCard(
    preset: ColorPreset,
    isSelected: Boolean,
    overrides: Map<String, Color>,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
    onShowDetails: () -> Unit,
) {
    val pt = overrides["primaryText"] ?: preset.primaryText
    val st = overrides["secondaryText"] ?: preset.secondaryText
    val pb = overrides["primaryBackground"] ?: preset.primaryBackground
    val sb = overrides["secondaryBackground"] ?: preset.secondaryBackground
    val ac = overrides["accent"] ?: preset.accent

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(pb)
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .combinedClickable(onClick = onSelect, onLongClick = onShowDetails)
            .padding(10.dp),
    ) {
        Text(
            preset.name,
            style = MaterialTheme.typography.labelSmall,
            color = pt,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorDot(pt); ColorDot(st); ColorDot(pb.copy(alpha = 1f))
            ColorDot(sb); ColorDot(ac)
        }
    }
}

@Composable
private fun ColorDot(color: Color, size: Int = 16) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .border(0.5.dp, Color.Gray.copy(alpha = 0.4f), CircleShape),
    )
}

// ─── Preset detail dialog ────────────────────────────────────────────────────

@Composable
private fun PresetDetailDialog(
    preset: ColorPreset,
    currentOverrides: Map<String, Color>,
    onDismiss: () -> Unit,
    onColorChanged: (String, Color) -> Unit,
) {
    var editingKey by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(preset.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSwatchRow(
                    "主要文字",
                    currentOverrides["primaryText"] ?: preset.primaryText,
                ) { editingKey = "primaryText" }
                ColorSwatchRow(
                    "次要文字",
                    currentOverrides["secondaryText"] ?: preset.secondaryText,
                ) { editingKey = "secondaryText" }
                ColorSwatchRow(
                    "主要背景",
                    currentOverrides["primaryBackground"] ?: preset.primaryBackground,
                ) { editingKey = "primaryBackground" }
                ColorSwatchRow(
                    "次要背景",
                    currentOverrides["secondaryBackground"] ?: preset.secondaryBackground,
                ) { editingKey = "secondaryBackground" }
                ColorSwatchRow(
                    "强调色",
                    currentOverrides["accent"] ?: preset.accent,
                ) { editingKey = "accent" }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        },
    )

    editingKey?.let { key ->
        val initial = currentOverrides[key] ?: run {
            when (key) {
                "primaryText" -> preset.primaryText
                "secondaryText" -> preset.secondaryText
                "primaryBackground" -> preset.primaryBackground
                "secondaryBackground" -> preset.secondaryBackground
                else -> preset.accent
            }
        }
        HsvColorPickerDialog(
            initialColor = initial,
            label = when (key) {
                "primaryText" -> "主要文字"
                "secondaryText" -> "次要文字"
                "primaryBackground" -> "主要背景"
                "secondaryBackground" -> "次要背景"
                else -> "强调色"
            },
            onDismiss = { editingKey = null },
            onColorSelected = { color ->
                onColorChanged(key, color)
                editingKey = null
            },
        )
    }
}

// ─── Color swatch row ────────────────────────────────────────────────────────

@Composable
private fun ColorSwatchRow(label: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            colorToHexString(color),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─── HSV color picker dialog ─────────────────────────────────────────────────

@Composable
private fun HsvColorPickerDialog(
    initialColor: Color,
    label: String,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit,
) {
    val initialHsv = remember(initialColor) { colorToHsv(initialColor) }
    var h by remember { mutableFloatStateOf(initialHsv[0]) }
    var s by remember { mutableFloatStateOf(initialHsv[1]) }
    var v by remember { mutableFloatStateOf(initialHsv[2]) }
    val previewColor = remember(h, s, v) { hsvToColor(h, s, v) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label) },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(previewColor)
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                )
                Spacer(Modifier.height(20.dp))
                HsvSliderRow("色相", h, 0f..360f) { h = it }
                Spacer(Modifier.height(10.dp))
                HsvSliderRow("饱和度", s, 0f..1f) { s = it }
                Spacer(Modifier.height(10.dp))
                HsvSliderRow("明度", v, 0f..1f) { v = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(previewColor) }) {
                Text(stringResource(Res.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        },
    )
}

@Composable
private fun HsvSliderRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(52.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (range.endInclusive > 10f) value.roundToInt().toString() else "${(value * 100).roundToInt() / 100f}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(40.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─── Text size slider ────────────────────────────────────────────────────────

@Composable
private fun TextSizeSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${value.roundToInt()}px",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = 19, // 20 discrete steps (5..25)
        )
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun byteToHex(byte: Int): String {
    val hex = "0123456789ABCDEF"
    val b = byte and 0xFF
    return "${hex[b / 16]}${hex[b % 16]}"
}

private fun colorToHexString(color: Color): String {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()
    return "#${byteToHex(r)}${byteToHex(g)}${byteToHex(b)}"
}
