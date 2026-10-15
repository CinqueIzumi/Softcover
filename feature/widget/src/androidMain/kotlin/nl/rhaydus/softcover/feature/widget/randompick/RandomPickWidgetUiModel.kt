package nl.rhaydus.softcover.feature.widget.randompick

import android.graphics.Bitmap
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.backdrop.BackdropArtwork

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
