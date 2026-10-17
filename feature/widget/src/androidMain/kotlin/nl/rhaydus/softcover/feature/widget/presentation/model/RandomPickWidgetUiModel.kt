package nl.rhaydus.softcover.feature.widget.presentation.model

import android.net.Uri
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration

internal data class RandomPickWidgetUiModel(
    val themeConfiguration: ThemeConfiguration,
    val content: Content,
) {
    sealed interface Content {
        data object Loading : Content

        /** Shown while the new settings resolve, with the [eyebrow] already naming them. */
        data class Switching(val eyebrow: String) : Content

        data object SignedOut : Content

        data object SourceUnavailable : Content

        data class EmptyPool(val eyebrow: String) : Content

        /** [artwork] is `null` while its files are being prepared, and the backdrop is left out. */
        data class Picked(
            val eyebrow: String,
            val bookId: Int,
            val title: String,
            val author: String,
            val cover: Cover,
            val artwork: Artwork?,
        ) : Content
    }

    sealed interface Cover {
        /** The cover file is still being prepared; render the placeholder. */
        data object Pending : Cover

        data object Missing : Cover

        data class Loaded(val uri: Uri) : Cover
    }

    /** [backdrop] is `null` while the book has no cover. */
    data class Artwork(
        val backdrop: Uri?,
        val veil: Uri,
        val veilLift: Uri,
        val washPrimary: Uri,
        val washTertiary: Uri,
    )
}
