package nl.rhaydus.softcover.feature.widget.presentation.model

import android.graphics.Bitmap
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropArtwork

internal data class RandomPickWidgetUiModel(
    val themeConfiguration: ThemeConfiguration,
    val content: Content,
) {
    sealed interface Content {
        data object Loading : Content

        data object SignedOut : Content

        data class EmptyPool(val eyebrow: String) : Content

        data class Picked(
            val eyebrow: String,
            val bookId: Int,
            val title: String,
            val author: String,
            val cover: Bitmap?,
            val artwork: BackdropArtwork?,
        ) : Content
    }
}
