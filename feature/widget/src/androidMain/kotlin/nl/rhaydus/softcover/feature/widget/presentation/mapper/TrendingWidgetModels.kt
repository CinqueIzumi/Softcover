package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.net.Uri
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition
import nl.rhaydus.softcover.feature.widget.domain.util.TRENDING_MAX_ITEMS
import nl.rhaydus.softcover.feature.widget.domain.util.TRENDING_PAGE_SIZE
import nl.rhaydus.softcover.feature.widget.domain.util.nextPage
import nl.rhaydus.softcover.feature.widget.domain.util.pageCount
import nl.rhaydus.softcover.feature.widget.domain.util.resolveShownPage
import nl.rhaydus.softcover.feature.widget.presentation.model.TrendingWidgetUiModel
import kotlin.time.Duration.Companion.milliseconds

private class TrendingWidgetInputs(
    val isSignedIn: Boolean,
    val themeConfiguration: ThemeConfiguration,
    val position: TrendingPosition,
    val books: List<Book>,
)

/**
 * [position] re-emits on every settings or page change, because a live Glance session does not re-run
 * `provideGlance`. [observeTrending] arrives ordered by rank.
 *
 * Each input emits the page with its covers pending, then again with their [coverUri]s. Covers that resolve
 * within [COVER_HIT_WINDOW] skip the pending step. Once the page is shown, the neighbouring pages' covers are
 * resolved in the background so a page turn finds them ready.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun trendingWidgetModels(
    isSignedIn: Flow<Boolean>,
    themeConfiguration: Flow<ThemeConfiguration>,
    position: Flow<TrendingPosition>,
    observeTrending: (TrendingPeriod) -> Flow<List<Book>>,
    eyebrowFor: (TrendingPeriod) -> String,
    coverUri: suspend (Book) -> Uri?,
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
    }.transformLatest { inputs ->
        if (inputs.isSignedIn.not()) {
            emit(inputs.model(TrendingWidgetUiModel.Content.SignedOut))
            return@transformLatest
        }

        val eyebrow = eyebrowFor(inputs.position.period)

        if (inputs.books.isEmpty()) {
            emit(inputs.model(TrendingWidgetUiModel.Content.Empty(eyebrow = eyebrow)))
            return@transformLatest
        }

        val top = inputs.books.take(TRENDING_MAX_ITEMS)
        val pages = pageCount(top.size)

        val page = resolveShownPage(
            stored = inputs.position.page,
            pageCount = pages,
        )

        coroutineScope {
            val shown = top.pageBooks(page)
            val covers = shown.map { book -> async { coverUri(book) } }

            val ready = withTimeoutOrNull(COVER_HIT_WINDOW) { covers.awaitAll() }

            val resolved = ready ?: run {
                emit(
                    inputs.model(
                        ranked(
                            eyebrow = eyebrow,
                            page = page,
                            pages = pages,
                            top = top,
                            covers = List(shown.size) { TrendingWidgetUiModel.Cover.Pending },
                        ),
                    ),
                )

                covers.awaitAll()
            }

            emit(
                inputs.model(
                    ranked(
                        eyebrow = eyebrow,
                        page = page,
                        pages = pages,
                        top = top,
                        covers = resolved.map { it.toCover() },
                    ),
                ),
            )

            listOf(-1, 1)
                .map { step ->
                    nextPage(
                        stored = page,
                        pageCount = pages,
                        step = step,
                    )
                }
                .distinct()
                .filter { it != page }
                .flatMap { top.pageBooks(it) }
                .forEach { book -> launch { coverUri(book) } }
        }
    }
}

private fun TrendingWidgetInputs.model(content: TrendingWidgetUiModel.Content) = TrendingWidgetUiModel(
    themeConfiguration = themeConfiguration,
    content = content,
)

private fun List<Book>.pageBooks(page: Int): List<Book> = drop(page * TRENDING_PAGE_SIZE).take(TRENDING_PAGE_SIZE)

private fun Uri?.toCover(): TrendingWidgetUiModel.Cover = if (this == null) {
    TrendingWidgetUiModel.Cover.Missing
} else {
    TrendingWidgetUiModel.Cover.Loaded(uri = this)
}

private fun ranked(
    eyebrow: String,
    page: Int,
    pages: Int,
    top: List<Book>,
    covers: List<TrendingWidgetUiModel.Cover>,
): TrendingWidgetUiModel.Content.Ranked {
    val firstRank = page * TRENDING_PAGE_SIZE

    val items = top.pageBooks(page).mapIndexed { index, book ->
        TrendingWidgetUiModel.Item(
            bookId = book.id,
            rank = "${firstRank + index + 1}",
            title = book.title,
            author = book.authorString,
            cover = covers[index],
        )
    }

    val hasPages = pages > 1

    return TrendingWidgetUiModel.Content.Ranked(
        eyebrow = eyebrow,
        items = items.toImmutableList(),
        pageLabel = if (hasPages) "${page + 1}/$pages" else null,
        showsArrows = hasPages,
    )
}

private val COVER_HIT_WINDOW = 60.milliseconds
