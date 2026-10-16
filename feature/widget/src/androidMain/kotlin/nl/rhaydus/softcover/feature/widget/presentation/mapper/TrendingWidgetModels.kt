package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.graphics.Bitmap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition
import nl.rhaydus.softcover.feature.widget.presentation.model.TrendingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.util.TRENDING_MAX_ITEMS
import nl.rhaydus.softcover.feature.widget.presentation.util.TRENDING_PAGE_SIZE
import nl.rhaydus.softcover.feature.widget.presentation.util.pageCount
import nl.rhaydus.softcover.feature.widget.presentation.util.resolveShownPage

private class TrendingWidgetInputs(
    val isSignedIn: Boolean,
    val themeConfiguration: ThemeConfiguration,
    val position: TrendingPosition,
    val books: List<Book>,
)

/**
 * [position] re-emits on every settings or page change, because a live Glance session does not re-run
 * `provideGlance`. [observeTrending] arrives ordered by rank.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun trendingWidgetModels(
    isSignedIn: Flow<Boolean>,
    themeConfiguration: Flow<ThemeConfiguration>,
    position: Flow<TrendingPosition>,
    observeTrending: (TrendingPeriod) -> Flow<List<Book>>,
    eyebrowFor: (TrendingPeriod) -> String,
    loadCover: suspend (Book) -> Bitmap?,
): Flow<TrendingWidgetUiModel> {
    val positionedBooks = position.flatMapLatest { current ->
        observeTrending(current.period).map { books -> current to books }
    }

    return combine(
        isSignedIn,
        themeConfiguration,
        positionedBooks,
    ) { signedIn, theme, (current, books) ->
        TrendingWidgetInputs(
            isSignedIn = signedIn,
            themeConfiguration = theme,
            position = current,
            books = books,
        )
    }.mapLatest { inputs ->
        TrendingWidgetUiModel(
            themeConfiguration = inputs.themeConfiguration,
            content = inputs.toContent(
                eyebrowFor = eyebrowFor,
                loadCover = loadCover,
            ),
        )
    }
}

private suspend fun TrendingWidgetInputs.toContent(
    eyebrowFor: (TrendingPeriod) -> String,
    loadCover: suspend (Book) -> Bitmap?,
): TrendingWidgetUiModel.Content {
    if (isSignedIn.not()) return TrendingWidgetUiModel.Content.SignedOut

    val eyebrow = eyebrowFor(position.period)

    if (books.isEmpty()) return TrendingWidgetUiModel.Content.Empty(eyebrow = eyebrow)

    val top = books.take(TRENDING_MAX_ITEMS)
    val pages = pageCount(top.size)

    val page = resolveShownPage(
        stored = position.page,
        pageCount = pages,
    )

    val firstRank = page * TRENDING_PAGE_SIZE

    val items = coroutineScope {
        top
            .drop(firstRank)
            .take(TRENDING_PAGE_SIZE)
            .mapIndexed { index, book ->
                async {
                    TrendingWidgetUiModel.Item(
                        bookId = book.id,
                        rank = "${firstRank + index + 1}",
                        title = book.title,
                        author = book.authorString,
                        cover = loadCover(book),
                    )
                }
            }
            .awaitAll()
    }

    val hasPages = pages > 1

    return TrendingWidgetUiModel.Content.Ranked(
        eyebrow = eyebrow,
        items = items.toImmutableList(),
        pageLabel = if (hasPages) "${page + 1}/$pages" else null,
        showsArrows = hasPages,
    )
}
