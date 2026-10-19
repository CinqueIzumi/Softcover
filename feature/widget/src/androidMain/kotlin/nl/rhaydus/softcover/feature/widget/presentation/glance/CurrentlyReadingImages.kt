package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import android.net.Uri
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.cover.loadCoverBitmap
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageFormat
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageStore
import nl.rhaydus.softcover.feature.widget.presentation.model.BookArtworkUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropImages
import nl.rhaydus.softcover.feature.widget.presentation.render.WavyProgressUris
import nl.rhaydus.softcover.feature.widget.presentation.render.coverSource
import nl.rhaydus.softcover.feature.widget.presentation.render.keyPart
import nl.rhaydus.softcover.feature.widget.presentation.render.renderWavyProgress
import nl.rhaydus.softcover.feature.widget.presentation.util.PROGRESS_BAR_HEIGHT_DP
import kotlin.math.roundToInt

/** Stores every image of one widget instance as a file and hands out its [Uri], keyed by what changes the pixels. */
internal class CurrentlyReadingImages(
    private val store: WidgetImageStore,
    private val context: Context,
    private val backdrops: BackdropImages,
    private val density: Float,
) {
    suspend fun coverUri(
        book: Book,
        coverHeightDp: Int,
    ): Uri? {
        val source = book.coverSource() ?: return null
        val sizePx = (coverHeightDp * density).roundToInt()

        return store.uriFor(
            format = WidgetImageFormat.Photo,
            key = "cr-cover:v2:${source.keyPart()}:$sizePx",
        ) {
            loadCoverBitmap(
                context = context,
                source = source,
                sizePx = sizePx,
            )
        }
    }

    suspend fun artworkUris(book: Book): BookArtworkUiModel? = backdrops.artwork(
        source = book.coverSource(),
    ) { backdrop, veil, veilLift, washPrimary, washTertiary ->
        BookArtworkUiModel(
            backdrop = backdrop,
            veil = veil,
            veilLift = veilLift,
            washPrimary = washPrimary,
            washTertiary = washTertiary,
        )
    }

    suspend fun progressUris(
        fraction: Float,
        widthDp: Int,
    ): WavyProgressUris? {
        val widthPx = (widthDp * density).roundToInt()
        val heightPx = (PROGRESS_BAR_HEIGHT_DP * density).roundToInt()
        val inputs = "${widthPx}x$heightPx:$fraction:$density"

        val bitmaps = lazy {
            renderWavyProgress(
                widthPx = widthPx,
                heightPx = heightPx,
                density = density,
                fraction = fraction,
            )
        }

        return WavyProgressUris(
            track = store.uriFor(key = "cr-wave-argb:track:$inputs") { bitmaps.value.track } ?: return null,
            indicator = store.uriFor(key = "cr-wave-argb:indicator:$inputs") { bitmaps.value.indicator } ?: return null,
        )
    }
}
