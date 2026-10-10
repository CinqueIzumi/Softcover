package nl.rhaydus.softcover.core.component.text

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * A [SoftcoverText] that fades out and back in on the new [text] when it changes between two loaded
 * values, and snaps under reduced motion. The end of [isLoading] is not a change: the skeleton mask
 * reveals the text as usual.
 */
@Composable
fun FadeThroughText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    standIn: String = "",
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val spec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    var shown by remember { mutableStateOf(text) }
    var settled by remember { mutableStateOf(isLoading.not()) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(text, isLoading) {
        if (isLoading.not() && settled && text != shown) {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = spec,
            )

            shown = text

            alpha.animateTo(
                targetValue = 1f,
                animationSpec = spec,
            )
        } else {
            shown = text
            alpha.snapTo(1f)
        }

        settled = isLoading.not()
    }

    SoftcoverText(
        text = if (isLoading || settled.not()) text else shown,
        style = style,
        color = color,
        isLoading = isLoading,
        standIn = standIn,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier.graphicsLayer { this.alpha = alpha.value },
    )
}
