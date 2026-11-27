package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.DpSize
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.cover.loadCoverBitmap
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageFormat
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageStore
import nl.rhaydus.softcover.feature.widget.presentation.model.BookArtworkUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropImages
import nl.rhaydus.softcover.feature.widget.presentation.render.WavyProgressImages
import nl.rhaydus.softcover.feature.widget.presentation.render.coverSource
import nl.rhaydus.softcover.feature.widget.presentation.render.keyPart
import nl.rhaydus.softcover.feature.widget.presentation.render.renderWavyProgress
import nl.rhaydus.softcover.feature.widget.presentation.util.BYTES_PER_PIXEL
import nl.rhaydus.softcover.feature.widget.presentation.util.PROGRESS_BAR_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.fittingCoverHeightDp
import nl.rhaydus.softcover.feature.widget.presentation.util.progressBarWidthsDp
import kotlin.math.roundToInt

/** Stores every image of one widget instance as a file and hands out its [Bitmap], keyed by what changes the pixels. */
internal class CurrentlyReadingImages(
    private val store: WidgetImageStore,
    private val context: Context,
    private val backdrops: BackdropImages,
    private val density: Float,
) {
    /**
     * [requestedDp] lowered when [sizes] copies of the cover, backdrop and progress bars would overflow the update
     * cap.
     */
    fun fitCoverHeightDp(
        requestedDp: Int,
        sizes: List<DpSize>,
    ): Int {
        val barPx = (progressBarWidthsDp(sizes).max() * density).roundToInt()
        val barBytes = 2L * barPx * (PROGRESS_BAR_HEIGHT_DP * density).roundToInt() * BYTES_PER_PIXEL

        return fittingCoverHeightDp(
            requestedDp = requestedDp,
            density = density,
            sizeCount = sizes.size,
            fixedBytesPerSize = backdrops.bytesPerSize + barBytes,
            coversPerSize = 1,
        )
    }

    suspend fun coverBitmap(
        book: Book,
        coverHeightDp: Int,
    ): Bitmap? {
        val source = book.coverSource() ?: return null

        val sizePx = (coverHeightDp * density).roundToInt()

        return store.bitmapFor(
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

    suspend fun artwork(book: Book): BookArtworkUiModel? = backdrops.artwork(
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

    suspend fun progressImages(
        fraction: Float,
        widthDp: Int,
    ): WavyProgressImages? {
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

        return WavyProgressImages(
            track = store.bitmapFor(key = "cr-wave-argb:track:$inputs") { bitmaps.value.track } ?: return null,
            indicator = store.bitmapFor(key = "cr-wave-argb:indicator:$inputs") { bitmaps.value.indicator } ?: return null,
        )
    }
}
