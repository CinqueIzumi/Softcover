package nl.rhaydus.softcover.feature.widget.presentation.render

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.cover.loadCoverBitmap
import nl.rhaydus.softcover.core.uibinding.cover.resolveCoverSource
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

    /** Backdrops are blurred, so half resolution is invisible once Crop scales them up, and a quarter of the bytes. */
    private val renderDensity = density * BACKDROP_SCALE

    private val backdropWidthPx = (BACKDROP_WIDTH_DP * renderDensity).roundToInt()
    private val backdropHeightPx = (BACKDROP_HEIGHT_DP * renderDensity).roundToInt()

    private val masks by lazy { renderGradientMasks() }

    suspend fun coverUri(book: Book): Uri? {
        val source = book.coverSource() ?: return null

        return store.uriFor(
            key = "rp-cover:${source.keyPart()}:$coverPx",
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
    suspend fun artwork(book: Book): RandomPickWidgetUiModel.Artwork? = coroutineScope {
        val backdrop = async { backdropUri(book) }
        val veil = async { maskUri(name = "veil") { masks.veil } }
        val veilLift = async { maskUri(name = "veilLift") { masks.veilLift } }
        val washPrimary = async { maskUri(name = "washPrimary") { masks.washPrimary } }
        val washTertiary = async { maskUri(name = "washTertiary") { masks.washTertiary } }

        RandomPickWidgetUiModel.Artwork(
            backdrop = backdrop.await(),
            veil = veil.await() ?: return@coroutineScope null,
            veilLift = veilLift.await() ?: return@coroutineScope null,
            washPrimary = washPrimary.await() ?: return@coroutineScope null,
            washTertiary = washTertiary.await() ?: return@coroutineScope null,
        )
    }

    private suspend fun backdropUri(book: Book): Uri? {
        val source = book.coverSource() ?: return null

        return store.uriFor(
            key = "backdrop:${source.keyPart()}:${backdropWidthPx}x$backdropHeightPx",
            render = {
                loadCoverBitmap(
                    context = context,
                    source = source,
                    sizePx = coverPx,
                )?.let { cover ->
                    renderBackdrop(
                        cover = cover,
                        widthPx = backdropWidthPx,
                        heightPx = backdropHeightPx,
                        sigmaPx = SKIA_SIGMA_PER_RADIUS * BLUR_DP * renderDensity + SKIA_SIGMA_OFFSET,
                    )
                }
            },
        )
    }

    private suspend fun maskUri(
        name: String,
        render: () -> Bitmap,
    ): Uri? = store.uriFor(
        key = "mask:$name:$MASK_ROWS",
        render = render,
    )

    private fun Book.coverSource(): CoverSource? = resolveCoverSource(
        edition = currentEdition,
        defaultEdition = defaultEdition,
        fallbackCoverUrl = coverUrl,
    )

    private fun CoverSource.keyPart(): String = when (this) {
        is CoverSource.Local -> path
        is CoverSource.Remote -> url
    }

    private companion object {
        const val BACKDROP_SCALE = 0.5f
        const val BACKDROP_WIDTH_DP = 110
        const val BACKDROP_HEIGHT_DP = 180

        /** The radius `Modifier.blur` would take; tuned lighter than the Reading hero's 6dp for the widget. */
        const val BLUR_DP = 2f

        /** Skia's blur radius to sigma conversion, which `RenderEffect.createBlurEffect` goes through. */
        const val SKIA_SIGMA_PER_RADIUS = 0.57735f
        const val SKIA_SIGMA_OFFSET = 0.5f

        /** Keys the mask files; keep equal to the row count `renderGradientMasks` renders with. */
        const val MASK_ROWS = 128
    }
}
