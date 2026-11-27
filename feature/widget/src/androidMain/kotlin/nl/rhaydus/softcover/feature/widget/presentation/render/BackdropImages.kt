package nl.rhaydus.softcover.feature.widget.presentation.render

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.uibinding.cover.loadCoverBitmap
import nl.rhaydus.softcover.core.uibinding.cover.resolveCoverSource
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageFormat
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageStore
import nl.rhaydus.softcover.feature.widget.presentation.util.BYTES_PER_PIXEL
import kotlin.math.roundToInt

/** Stores the blurred backdrop and the gradient masks behind a book as files, reusing a file whose key already exists. */
internal class BackdropImages(
    private val context: Context,
    density: Float,
    private val store: WidgetImageStore,
) {
    /** Backdrops are blurred, so half resolution is invisible once Crop scales them up, and a quarter of the bytes. */
    private val renderDensity = density * BACKDROP_SCALE

    private val backdropWidthPx = (BACKDROP_WIDTH_DP * renderDensity).roundToInt()
    private val backdropHeightPx = (BACKDROP_HEIGHT_DP * renderDensity).roundToInt()

    private val masks by lazy { renderGradientMasks() }

    /** What the backdrop and the four masks of one size weigh in a widget update. */
    val bytesPerSize: Long =
        (backdropWidthPx.toLong() * backdropHeightPx + MASK_COUNT * GRADIENT_WIDTH * GRADIENT_ROWS) * BYTES_PER_PIXEL

    /**
     * Hands the five stored images to [build], or returns `null` when a gradient file could not be written,
     * since the backdrop is not worth drawing without them. The backdrop is `null` while [source] is `null`.
     */
    suspend fun <T> artwork(
        source: CoverSource?,
        build: (backdrop: Bitmap?, veil: Bitmap, veilLift: Bitmap, washPrimary: Bitmap, washTertiary: Bitmap) -> T,
    ): T? = coroutineScope {
        val backdrop = async { backdropBitmap(source) }
        val veil = async { maskBitmap(name = "veil") { masks.veil } }
        val veilLift = async { maskBitmap(name = "veilLift") { masks.veilLift } }
        val washPrimary = async { maskBitmap(name = "washPrimary") { masks.washPrimary } }
        val washTertiary = async { maskBitmap(name = "washTertiary") { masks.washTertiary } }

        build(
            backdrop.await(),
            veil.await() ?: return@coroutineScope null,
            veilLift.await() ?: return@coroutineScope null,
            washPrimary.await() ?: return@coroutineScope null,
            washTertiary.await() ?: return@coroutineScope null,
        )
    }

    private suspend fun backdropBitmap(source: CoverSource?): Bitmap? {
        if (source == null) return null

        return store.bitmapFor(
            format = WidgetImageFormat.Photo,
            key = "backdrop:v$BACKDROP_RENDER_VERSION:${source.keyPart()}:${backdropWidthPx}x$backdropHeightPx",
            render = {
                loadCoverBitmap(
                    context = context,
                    source = source,
                    sizePx = backdropHeightPx,
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

    private suspend fun maskBitmap(
        name: String,
        render: () -> Bitmap,
    ): Bitmap? = store.bitmapFor(
        key = "mask:v$MASK_RENDER_VERSION:$name:$GRADIENT_ROWS",
        render = render,
    )

    private companion object {
        /** Bump whenever `GradientMasksRenderer`'s stops change, or stored masks keep the old look. */
        const val MASK_RENDER_VERSION = 1

        const val MASK_COUNT = 4

        /** Bump whenever the blur constants or `renderBackdrop` change, or stored backdrops keep the old look. */
        const val BACKDROP_RENDER_VERSION = 3

        const val BACKDROP_SCALE = 0.5f
        const val BACKDROP_WIDTH_DP = 110
        const val BACKDROP_HEIGHT_DP = 180

        /** The radius `Modifier.blur` would take; tuned lighter than the Reading hero's 6dp for the widget. */
        const val BLUR_DP = 2f

        /** Skia's blur radius to sigma conversion, which `RenderEffect.createBlurEffect` goes through. */
        const val SKIA_SIGMA_PER_RADIUS = 0.57735f
        const val SKIA_SIGMA_OFFSET = 0.5f
    }
}

/** Where the book's cover comes from, or `null` when it has none. */
internal fun Book.coverSource(edition: BookEdition? = currentEdition): CoverSource? = resolveCoverSource(
    edition = edition,
    defaultEdition = defaultEdition,
    fallbackCoverUrl = coverUrl,
)

/** The part of a stored cover's file key that says which cover it is. */
internal fun CoverSource.keyPart(): String = when (this) {
    is CoverSource.Local -> path
    is CoverSource.Remote -> url
}
