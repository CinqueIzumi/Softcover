package nl.rhaydus.softcover.core.designsystem.preset

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import java.lang.Float as JavaFloat
import nl.rhaydus.softcover.core.designsystem.presentation.theme.CornerRadius
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import kotlin.math.roundToInt

private const val PROBE_SIZE = 100f

/** The `spacing` and `radius` sections, read from the same objects the app lays out with. */
internal object SpacingTokens {
    private val spacingSteps: Map<String, Pair<Float, String>> = steps(
        type = Spacing::class.java,
        receiver = Spacing,
        prefix = "space",
        usage = SpacingUsage.spacing,
    )

    private val radiusSteps: Map<String, Pair<Float, String>> = steps(
        type = CornerRadius::class.java,
        receiver = CornerRadius,
        prefix = "radius",
        usage = SpacingUsage.radius,
    )

    private val pillRadius: String = Corners.pill.topStart
        .toPx(
            Size(
                PROBE_SIZE,
                PROBE_SIZE,
            ),
            Density(1f),
        )
        .let { "${it.roundToInt()}%" }

    fun spacing(): Map<String, Any?> = linkedMapOf("tokens" to dpTokens(spacingSteps))

    fun radius(): Map<String, Any?> = linkedMapOf(
        "tokens" to dpTokens(radiusSteps) + token(
            name = "radius-pill",
            value = pillRadius,
            usage = "Fully rounded ends: pill chips and buttons. True circles keep a circle shape.",
        ),
    )

    private fun steps(
        type: Class<*>,
        receiver: Any,
        prefix: String,
        usage: Map<String, String>,
    ): Map<String, Pair<Float, String>> = orderedByUsage(
        found = readPublicGetters(
            type,
            receiver,
            JavaFloat.TYPE,
        ) { it as Float },
        documented = usage.keys,
        source = type.simpleName,
    ).entries.associate { (name, value) -> "$prefix-$name" to (value to usage.getValue(name)) }

    private fun dpTokens(steps: Map<String, Pair<Float, String>>): List<Map<String, Any?>> = steps.map { (name, step) ->
        token(
            name = name,
            value = step.first.toPx(),
            usage = step.second,
        )
    }

    private fun token(
        name: String,
        value: String,
        usage: String,
    ): Map<String, Any?> = linkedMapOf("name" to name, "value" to value, "usage" to usage)
}
