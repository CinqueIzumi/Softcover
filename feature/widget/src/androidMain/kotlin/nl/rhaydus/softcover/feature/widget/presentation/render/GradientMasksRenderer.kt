package nl.rhaydus.softcover.feature.widget.presentation.render

/**
 * The stops are `FeaturedBackdropCard`'s blurred branch. Its extra 0.15 on light surfaces is a second
 * ramp ([GradientMasks.veilLift]) stacked on the first, sized so the two together reach that alpha.
 */
internal fun renderGradientMasks(): GradientMasks = GradientMasks(
    veil = renderAlphaGradient(
        stops = listOf(
            0f to 0.50f,
            0.4f to 0.58f,
            0.55f to 0.82f,
            1f to 0.92f,
        ),
        rows = GRADIENT_ROWS,
    ),
    veilLift = renderAlphaGradient(
        stops = listOf(
            0f to 0.30f,
            0.4f to 0.357f,
            0.55f to 0f,
            1f to 0f,
        ),
        rows = GRADIENT_ROWS,
    ),
    washPrimary = renderAlphaGradient(
        stops = listOf(
            0f to 0.45f,
            1f to 0f,
        ),
        rows = GRADIENT_ROWS,
    ),
    washTertiary = renderAlphaGradient(
        stops = listOf(
            0f to 0f,
            1f to 0.30f,
        ),
        rows = GRADIENT_ROWS,
    ),
)

private const val GRADIENT_ROWS = 128
