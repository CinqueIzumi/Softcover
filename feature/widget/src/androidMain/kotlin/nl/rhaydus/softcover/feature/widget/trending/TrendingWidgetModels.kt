package nl.rhaydus.softcover.feature.widget.trending

import android.graphics.Bitmap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration

internal const val MAX_TRENDING_ITEMS = 5

private class WidgetInputs(
    val isSignedIn: Boolean,
    val themeConfiguration: ThemeConfiguration,
    val books: List<Book>,
)

/** [trending] arrives ordered by rank. */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun trendingWidgetModels(
    isSignedIn: Flow<Boolean>,
    themeConfiguration: Flow<ThemeConfiguration>,
    trending: Flow<List<Book>>,
    loadCover: suspend (Book) -> Bitmap?,
): Flow<TrendingWidgetUiModel> = combine(
    isSignedIn,
    themeConfiguration,
    trending,
    ::WidgetInputs,
).mapLatest { inputs ->
    TrendingWidgetUiModel(
        themeConfiguration = inputs.themeConfiguration,
        content = inputs.toContent(loadCover = loadCover),
    )
}

private suspend fun WidgetInputs.toContent(
    loadCover: suspend (Book) -> Bitmap?,
): TrendingWidgetUiModel.Content {
    if (isSignedIn.not()) return TrendingWidgetUiModel.Content.SignedOut

    if (books.isEmpty()) return TrendingWidgetUiModel.Content.Empty

    val items = coroutineScope {
        books
            .take(MAX_TRENDING_ITEMS)
            .mapIndexed { index, book ->
                async {
                    TrendingWidgetUiModel.Item(
                        bookId = book.id,
                        rank = "${index + 1}",
                        title = book.title,
                        author = book.authorString,
                        cover = loadCover(book),
                    )
                }
            }
            .awaitAll()
    }

    return TrendingWidgetUiModel.Content.Ranked(items = items.toImmutableList())
}
