package nl.rhaydus.softcover.feature.widget.presentation.render

import android.content.Context
import android.graphics.Bitmap
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.cover.loadCoverBitmap
import nl.rhaydus.softcover.core.uibinding.cover.resolveCoverSource
import java.util.Collections
import kotlin.math.roundToInt

/** Loads book covers for one widget instance, keeping the last [capacity] while their sources are unchanged. */
internal class BookCoverLoader(
    private val context: Context,
    private val density: Float,
    private val coverHeightDp: Int = DEFAULT_COVER_HEIGHT_DP,
    private val capacity: Int = 1,
) {
    private val loaded = Collections.synchronizedMap(LinkedHashMap<CoverSource, Bitmap>())

    suspend fun load(book: Book): Bitmap? {
        val source = resolveCoverSource(
            edition = book.currentEdition,
            defaultEdition = book.defaultEdition,
            fallbackCoverUrl = book.coverUrl,
        )

        return source?.let { loaded[it] }
            ?: loadCoverBitmap(
                context = context,
                source = source,
                sizePx = (coverHeightDp * density).roundToInt(),
            )?.also { bitmap ->
                source?.let {
                    remember(
                        source = it,
                        bitmap = bitmap,
                    )
                }
            }
    }

    private fun remember(
        source: CoverSource,
        bitmap: Bitmap,
    ) {
        synchronized(loaded) {
            loaded[source] = bitmap

            if (loaded.size > capacity) loaded.remove(loaded.keys.first())
        }
    }

    private companion object {
        const val DEFAULT_COVER_HEIGHT_DP = 86
    }
}
