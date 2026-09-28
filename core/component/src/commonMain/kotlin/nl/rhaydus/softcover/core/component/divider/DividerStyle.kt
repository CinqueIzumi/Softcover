package nl.rhaydus.softcover.core.component.divider

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val CENTER_GAP = 12.dp

internal data class HorizontalDividerStyle(
    val ink: DividerInk,
    val inset: Dp,
    val centerGap: Dp,
)

internal fun HorizontalDividerSurface.style(): HorizontalDividerStyle = when (this) {
    HorizontalDividerSurface.DebugRoutesRow -> HorizontalDividerStyle(
        ink = DividerInk.Outline,
        inset = 20.dp,
        centerGap = CENTER_GAP,
    )

    HorizontalDividerSurface.ReadingLifeShareCard -> HorizontalDividerStyle(
        ink = DividerInk.ContentTint(alpha = 0.25f),
        inset = 0.dp,
        centerGap = CENTER_GAP,
    )

    HorizontalDividerSurface.SettingsRoadmap -> HorizontalDividerStyle(
        ink = DividerInk.Outline,
        inset = 0.dp,
        centerGap = CENTER_GAP,
    )

    HorizontalDividerSurface.CreateListQuote -> HorizontalDividerStyle(
        ink = DividerInk.Outline,
        inset = 0.dp,
        centerGap = CENTER_GAP,
    )

    HorizontalDividerSurface.OnboardingOrTypeIt -> HorizontalDividerStyle(
        ink = DividerInk.Outline,
        inset = 0.dp,
        centerGap = CENTER_GAP,
    )
}

internal fun VerticalDividerSurface.ink(): DividerInk = when (this) {
    VerticalDividerSurface.WidePaneSeparator -> DividerInk.Outline
}
