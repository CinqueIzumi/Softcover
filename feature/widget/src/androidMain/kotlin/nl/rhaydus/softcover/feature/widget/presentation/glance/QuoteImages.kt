package nl.rhaydus.softcover.feature.widget.presentation.glance

import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.feature.widget.presentation.model.QuoteWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropImages

/**
 * Carries no budget clamp, unlike the cover widgets: its backdrop is rendered at a fixed size and its four masks are
 * fixed too, so nothing grows with the widget and only the count of reported sizes multiplies the bytes.
 */
internal class QuoteImages(
    private val backdrops: BackdropImages,
) {
    suspend fun artwork(quote: Quote): QuoteWidgetUiModel.Artwork? = backdrops.artwork(
        source = quote.editionCoverUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { url ->
                CoverSource.Remote(
                    url = url,
                    persistEditionId = null,
                )
            },
    ) { backdrop, veil, veilLift, washPrimary, washTertiary ->
        QuoteWidgetUiModel.Artwork(
            backdrop = backdrop,
            veil = veil,
            veilLift = veilLift,
            washPrimary = washPrimary,
            washTertiary = washTertiary,
        )
    }
}
