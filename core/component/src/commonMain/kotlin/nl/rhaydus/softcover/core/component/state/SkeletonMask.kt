package nl.rhaydus.softcover.core.component.state

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.shimmer

/**
 * Draws a shimmer in place of the content while [visible]. The content still measures and lays out,
 * so a stand-in value sizes the slot the real one will fill.
 */
@Composable
fun Modifier.skeletonMask(
    visible: Boolean,
    shape: Shape = RoundedCornerShape(4.dp),
): Modifier {
    if (visible.not()) return this

    return this
        .shimmer(shape = shape)
        .clearAndSetSemantics { }
}
