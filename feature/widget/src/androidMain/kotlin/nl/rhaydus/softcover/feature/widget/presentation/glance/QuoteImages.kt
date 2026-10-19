package nl.rhaydus.softcover.feature.widget.presentation.glance

import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.feature.widget.presentation.model.QuoteWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropImages

internal class QuoteImages(
    private val backdrops: BackdropImages,
) {
    suspend fun artworkUris(quote: Quote): QuoteWidgetUiModel.Artwork? = backdrops.artwork(
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
