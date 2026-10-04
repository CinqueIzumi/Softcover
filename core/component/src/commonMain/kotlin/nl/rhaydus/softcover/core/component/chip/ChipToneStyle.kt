package nl.rhaydus.softcover.core.component.chip

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerCover

internal fun ChipTone.style(
    scheme: ColorScheme,
    selected: Boolean,
): ChipStyle = when (this) {
    ChipTone.Tonal -> tonalStyle(
        scheme = scheme,
        selected = selected,
    )

    ChipTone.Choice -> if (selected) {
        ChipStyle(
            container = scheme.primary,
            ink = scheme.onPrimary,
            weight = FontWeight.SemiBold,
        )
    } else {
        tonalStyle(
            scheme = scheme,
            selected = false,
        )
    }

    ChipTone.Filled -> ChipStyle(
        container = scheme.primary,
        ink = scheme.onPrimary,
        weight = FontWeight.SemiBold,
    )

    ChipTone.Container -> ChipStyle(
        container = scheme.primaryContainer,
        ink = scheme.onPrimaryContainer,
        weight = FontWeight.SemiBold,
    )

    ChipTone.Outlined -> ChipStyle(
        container = Color.Transparent,
        ink = scheme.onSurfaceVariant,
        weight = FontWeight.Medium,
        border = ChipBorder.Solid(color = scheme.outlineVariant),
        mutedInk = scheme.onSurfaceVariant,
    )

    ChipTone.Dashed -> ChipStyle(
        container = Color.Transparent,
        ink = scheme.primary,
        weight = FontWeight.SemiBold,
        border = ChipBorder.Dashed(color = scheme.primary),
    )

    ChipTone.Spoiler -> ChipStyle(
        container = scheme.spoilerCover,
        ink = Color.Transparent,
        weight = FontWeight.Medium,
    )
}

private fun tonalStyle(
    scheme: ColorScheme,
    selected: Boolean,
): ChipStyle = if (selected) {
    ChipStyle(
        container = scheme.secondaryContainer,
        ink = scheme.onSecondaryContainer,
        weight = FontWeight.Medium,
    )
} else {
    ChipStyle(
        container = scheme.surfaceContainerHigh,
        ink = scheme.onSurface,
        weight = FontWeight.Medium,
        mutedInk = scheme.onSurfaceVariant,
    )
}
