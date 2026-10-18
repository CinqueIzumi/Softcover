package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.net.Uri
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeedFetchStatus
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettingsChange
import nl.rhaydus.softcover.feature.widget.domain.util.TRENDING_MAX_ITEMS
import nl.rhaydus.softcover.feature.widget.domain.util.TRENDING_PAGE_SIZE
import nl.rhaydus.softcover.feature.widget.domain.util.nextPage
import nl.rhaydus.softcover.feature.widget.domain.util.pageCount
import nl.rhaydus.softcover.feature.widget.domain.util.resolveShownPage
import nl.rhaydus.softcover.feature.widget.presentation.model.TrendingWidgetUiModel
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private class TrendingWidgetInputs(
    val isSignedIn: Boolean,
    val themeConfiguration: ThemeConfiguration,
    val position: TrendingPosition,
    val books: List<Book>,
    val fetchStatus: TrendingFeedFetchStatus,
    val settingsChange: TrendingSettingsChange?,
)

/**
 * [position] re-emits on every settings or page change, because a live Glance session does not re-run
 * `provideGlance`. [observeTrending] arrives ordered by rank.
 *
 * Each input emits the page with its covers pending, then again with their [coverUri]s. Covers that resolve
 * within [COVER_HIT_WINDOW] skip the pending step. Once the page is shown, the neighbouring pages' covers are
 * resolved in the background so a page turn finds them ready.
 *
 * An empty ranking shows the loader, with the eyebrow of the feed, until [fetchStatus] says the feed has had data,
 * so [TrendingWidgetUiModel.Content.Empty] means a fetch finished with nothing, and
 * [TrendingWidgetUiModel.Content.FetchFailed] one failed with nothing cached.
 *
 * A [settingsChanges] emission first shows the loading frame with the eyebrow for the new feed, then the
 * ranking once [position] has caught up. If it has not caught up within [SWITCH_TIMEOUT], the ranking for the
 * current position is shown instead, so the loader never sticks. Page turns and data refreshes never show the
 * loader.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun trendingWidgetModels(
    isSignedIn: Flow<Boolean>,
    themeConfiguration: Flow<ThemeConfiguration>,
    position: Flow<TrendingPosition>,
    settingsChanges: Flow<TrendingSettingsChange> = emptyFlow(),
    observeTrending: (TrendingFeed) -> Flow<List<Book>>,
    fetchStatus: (TrendingFeed) -> Flow<TrendingFeedFetchStatus>,
    eyebrowFor: (TrendingFeed) -> String,
    emptyHeadlineFor: (TrendingFeed) -> String,
    coverUri: suspend (Book) -> Uri?,
): Flow<TrendingWidgetUiModel> {
    val positionedBooks = position.flatMapLatest { current ->
        combine(
            observeTrending(current.feed),
            fetchStatus(current.feed),
        ) { books, status ->
            FeedBooks(
                position = current,
                books = books,
                fetchStatus = if (books.isEmpty()) status else TrendingFeedFetchStatus.Settled,
            )
        }.distinctUntilChanged()
    }

    return flow {
        var announced: TrendingSettingsChange? = null

        emitAll(
            combine(
                isSignedIn,
                themeConfiguration,
                positionedBooks,
                settingsChanges.map<TrendingSettingsChange, TrendingSettingsChange?> { it }.onStart { emit(null) },
            ) { signedIn, theme, feedBooks, settingsChange ->
                TrendingWidgetInputs(
                    isSignedIn = signedIn,
                    themeConfiguration = theme,
                    position = feedBooks.position,
                    books = feedBooks.books,
                    fetchStatus = feedBooks.fetchStatus,
                    settingsChange = settingsChange,
                )
            }.transformLatest { inputs ->
                val change = inputs.settingsChange

                if (inputs.isSignedIn && change != null && change !== announced) {
                    announced = change

                    emit(
                        inputs.model(
                            TrendingWidgetUiModel.Content.Switching(
                                eyebrow = eyebrowFor(change.settings.feed),
                            ),
                        ),
                    )

                    // Fresh inputs for the new feed cancel this wait; if none arrive the current ranking stands.
                    if (inputs.position.feed != change.settings.feed) delay(SWITCH_TIMEOUT)
                }

                emitRanking(
                    inputs = inputs,
                    eyebrowFor = eyebrowFor,
                    emptyHeadlineFor = emptyHeadlineFor,
                    coverUri = coverUri,
                )
            },
        )
    }
}

private suspend fun FlowCollector<TrendingWidgetUiModel>.emitRanking(
    inputs: TrendingWidgetInputs,
    eyebrowFor: (TrendingFeed) -> String,
    emptyHeadlineFor: (TrendingFeed) -> String,
    coverUri: suspend (Book) -> Uri?,
) {
    if (inputs.isSignedIn.not()) {
        emit(inputs.model(TrendingWidgetUiModel.Content.SignedOut))
        return
    }

    val eyebrow = eyebrowFor(inputs.position.feed)

    if (inputs.books.isEmpty()) {
        val content = when (inputs.fetchStatus) {
            TrendingFeedFetchStatus.Loading -> TrendingWidgetUiModel.Content.Switching(eyebrow = eyebrow)

            TrendingFeedFetchStatus.Failed -> TrendingWidgetUiModel.Content.FetchFailed(eyebrow = eyebrow)

            TrendingFeedFetchStatus.Settled -> TrendingWidgetUiModel.Content.Empty(
                eyebrow = eyebrow,
                headline = emptyHeadlineFor(inputs.position.feed),
            )
        }

        emit(inputs.model(content))
        return
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

private val SWITCH_TIMEOUT = 5.seconds
