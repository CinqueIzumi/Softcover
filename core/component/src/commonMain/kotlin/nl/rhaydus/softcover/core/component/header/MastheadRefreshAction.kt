package nl.rhaydus.softcover.core.component.header

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.header.MastheadRefreshEvent.Refresh
import nl.rhaydus.softcover.core.component.text.SoftcoverText

/** A [PageMasthead] trailing action: refreshes the page it names. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MastheadRefreshAction(
    model: MastheadRefreshUiModel,
    onEvent: (MastheadRefreshEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = { onEvent(Refresh) },
        enabled = model.enabled,
        modifier = modifier.pointerHandCursor(),
    ) {
        val fadeSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
        val sizeSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()

        AnimatedContent(
            targetState = model.label,
            transitionSpec = {
                fadeIn(fadeSpec) togetherWith
                    fadeOut(fadeSpec) using
                    SizeTransform(
                        clip = false,
                        sizeAnimationSpec = { _, _ -> sizeSpec },
                    )
            },
            label = "mastheadRefreshLabel",
        ) { label ->
            SoftcoverText(text = label)
        }
    }
}
