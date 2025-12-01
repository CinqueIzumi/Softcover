package nl.rhaydus.softcover.core.presentation.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource

sealed class SoftcoverIconResource(
    open val contentDescription: String,
) {
    data class Drawable(
        @field:DrawableRes val id: Int,
        override val contentDescription: String,
    ) : SoftcoverIconResource(contentDescription = contentDescription)

    data class Vector(
        val vector: ImageVector,
        override val contentDescription: String,
    ) : SoftcoverIconResource(contentDescription = contentDescription)

    @Composable
    fun getIconPainter(): Painter {
        return when (this) {
            is Drawable -> painterResource(id = id)
            is Vector -> rememberVectorPainter(image = vector)
        }
    }
}