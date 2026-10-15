package nl.rhaydus.softcover.feature.widget.backdrop

import android.content.Context
import android.graphics.Bitmap
import kotlin.math.roundToInt
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.cover.loadCoverBitmap
import nl.rhaydus.softcover.core.uibinding.cover.resolveCoverSource

internal class BookCoverLoader(
    private val context: Context,
    private val density: Float,
) {
    private var last: Pair<CoverSource, Bitmap>? = null

    suspend fun load(book: Book): Bitmap? {
        val source = resolveCoverSource(
            edition = book.currentEdition,
            defaultEdition = book.defaultEdition,
            fallbackCoverUrl = book.coverUrl,
        )

        return last?.takeIf { it.first == source }?.second
            ?: loadCoverBitmap(
                context = context,
                source = source,
                sizePx = (COVER_HEIGHT_DP * density).roundToInt(),
            )?.also { bitmap -> source?.let { last = it to bitmap } }
    }

    private companion object {
        const val COVER_HEIGHT_DP = 86
    }
}
