package nl.rhaydus.softcover.core.designsystem.presentation.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import org.jetbrains.compose.resources.painterResource

sealed class SoftcoverIconResource(
    open val contentDescription: String,
) {
    data class Drawable(
        override val contentDescription: String,
        internal val icon: SoftcoverIcon,
    ) : SoftcoverIconResource(contentDescription = contentDescription)

    data class Vector(
        override val contentDescription: String,
        internal val vector: ImageVector,
    ) : SoftcoverIconResource(contentDescription = contentDescription)

    data class SoftcoverPainter(
        override val contentDescription: String,
        internal val painter: Painter,
    ) : SoftcoverIconResource(contentDescription = contentDescription)

    @Composable
    fun getIconPainter(): Painter {
        return when (this) {
            is Drawable -> painterResource(icon.resource)
            is Vector -> rememberVectorPainter(image = vector)
            is SoftcoverPainter -> painter
        }
    }
}
