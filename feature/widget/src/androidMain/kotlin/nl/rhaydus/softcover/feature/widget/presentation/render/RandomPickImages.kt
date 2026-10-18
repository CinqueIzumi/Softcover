package nl.rhaydus.softcover.feature.widget.presentation.render

import android.content.Context
import android.net.Uri
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.cover.loadCoverBitmap
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageFormat
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageStore
import nl.rhaydus.softcover.feature.widget.presentation.model.RandomPickWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.util.maxRandomPickCoverHeightDp
import kotlin.math.roundToInt

/** Turns a book into the image files the Random pick widget shows, reusing a file whose key already exists. */
internal class RandomPickImages(
    private val context: Context,
    private val density: Float,
    private val store: WidgetImageStore,
) {
    private val coverPx = (maxRandomPickCoverHeightDp() * density).roundToInt()

    private val backdrops = BackdropImages(
        context = context,
        density = density,
        store = store,
    )

    suspend fun coverUri(book: Book): Uri? {
        val source = book.coverSource() ?: return null

        return store.uriFor(
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
    suspend fun artwork(book: Book): RandomPickWidgetUiModel.Artwork? = backdrops.artwork(
        book = book,
        coverHeightDp = maxRandomPickCoverHeightDp(),
    ) { backdrop, veil, veilLift, washPrimary, washTertiary ->
        RandomPickWidgetUiModel.Artwork(
            backdrop = backdrop,
            veil = veil,
            veilLift = veilLift,
            washPrimary = washPrimary,
            washTertiary = washTertiary,
        )
    }
}
