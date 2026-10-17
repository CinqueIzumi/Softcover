package nl.rhaydus.softcover.feature.widget.presentation.render

import kotlin.math.max
import kotlin.math.min

/**
 * Where `LinearWavyProgressIndicator` places the indicator, track and stop dot for a determinate bar at
 * Material 3 defaults, in pixels. Amplitude is the default step: full between 10% and 95%, flat outside.
 */
internal data class WavyProgressGeometry(
    val isWavy: Boolean,
    val strokeWidth: Float,
    val wavelength: Float,
    val indicatorStart: Float,
    val indicatorEnd: Float,
    val hasIndicator: Boolean,
    val trackStart: Float,
    val trackEnd: Float,
    val hasTrack: Boolean,
    val stopCenterX: Float,
    val stopRadius: Float,
)

internal fun wavyProgressGeometry(
    widthPx: Float,
    density: Float,
    fraction: Float,
): WavyProgressGeometry {
    val progress = fraction.coerceIn(
        minimumValue = 0f,
        maximumValue = 1f,
    )
    val strokeWidth = 4f * density
    val capInset = strokeWidth / 2f
    val gapSize = 4f * density

    val head = progress * widthPx
    val isHeadVisible = head >= capInset

    val indicatorEnd = head.coerceIn(
        minimumValue = capInset,
        maximumValue = widthPx - capInset,
    )

    val gap = if (isHeadVisible) {
        min(
            head - capInset,
            gapSize,
        )
    } else {
        0f
    }
    val trackSpacing = if (isHeadVisible) gap + capInset * 2f else gap

    var stopSize = strokeWidth
    var stopX = widthPx - stopSize
    val progressX = head + capInset

    if (stopX <= progressX) {
        stopSize = max(
            0f,
            stopSize - (progressX - stopX),
        )
        stopX = progressX
    }

    return WavyProgressGeometry(
        isWavy = progress > WAVE_START && progress < WAVE_END,
        strokeWidth = strokeWidth,
        wavelength = 40f * density,
        indicatorStart = capInset,
        indicatorEnd = indicatorEnd,
        hasIndicator = progress > 0f && indicatorEnd > capInset,
        trackStart = widthPx - capInset,
        trackEnd = max(
            capInset,
            indicatorEnd + trackSpacing,
        ),
        hasTrack = widthPx - capInset > indicatorEnd + trackSpacing,
        stopCenterX = stopX + stopSize / 2f,
        stopRadius = stopSize / 2f,
    )
}

private const val WAVE_START = 0.1f
private const val WAVE_END = 0.95f
