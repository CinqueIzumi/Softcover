package nl.rhaydus.softcover.feature.widget.presentation.render

import android.content.Context
import android.graphics.Bitmap
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.uibinding.cover.loadCoverBitmap
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickBook
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageFormat
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageStore
import nl.rhaydus.softcover.feature.widget.presentation.model.BookArtworkUiModel
import nl.rhaydus.softcover.feature.widget.presentation.util.fittingCoverHeightDp
import kotlin.math.roundToInt

/** Turns a book into the image files the Random pick widget shows, reusing a file whose key already exists. */
internal class RandomPickImages(
    private val context: Context,
    private val density: Float,
    private val store: WidgetImageStore,
) {
    private val backdrops = BackdropImages(
        context = context,
        density = density,
        store = store,
    )

    /** [requestedDp] lowered when [sizeCount] copies of the cover and backdrop would overflow the update cap. */
    fun fitCoverHeightDp(
        requestedDp: Int,
        sizeCount: Int,
    ): Int = fittingCoverHeightDp(
        requestedDp = requestedDp,
        density = density,
        sizeCount = sizeCount,
        fixedBytesPerSize = backdrops.bytesPerSize,
        coversPerSize = 1,
    )

    suspend fun coverBitmap(
        pick: RandomPickBook,
        coverHeightDp: Int,
    ): Bitmap? {
        val source = pick.coverSource() ?: return null

        val coverPx = (coverHeightDp * density).roundToInt()

        return store.bitmapFor(
            format = WidgetImageFormat.Photo,
            key = "rp-cover:v2:${source.keyPart()}:$coverPx",
            render = {
                loadCoverBitmap(
                    context = context,
                    source = source,
                    sizePx = coverPx,
                )
            },
        )
    }

    /** `null` when a gradient file could not be written, since the backdrop is not worth drawing without them. */
    suspend fun artwork(pick: RandomPickBook): BookArtworkUiModel? = backdrops.artwork(
        source = pick.coverSource(),
    ) { backdrop, veil, veilLift, washPrimary, washTertiary ->
        BookArtworkUiModel(
            backdrop = backdrop,
            veil = veil,
            veilLift = veilLift,
            washPrimary = washPrimary,
            washTertiary = washTertiary,
        )
    }
}

private fun RandomPickBook.coverSource(): CoverSource? = book.coverSource(edition = shownEdition)
