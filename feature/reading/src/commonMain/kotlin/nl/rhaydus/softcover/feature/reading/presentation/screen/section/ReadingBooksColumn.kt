package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.mutationAnimated
import nl.rhaydus.designsystem.component.rememberLazyItemMutationAnimator
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.core.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.reading.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnDismissPlanTodayAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnShowProgressSheetClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.screen.MarkAsReadController
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

/**
 * The scrolling currently-reading column shared by both platforms: a [header] slot (the mobile
 * collapsing header vs. the desktop static one), the featured card (which fuses its own
 * plan-today/pace nudge to its top edge), and the "also reading" list. The hosting [listState] and
 * [contentPadding] are supplied by each layout so desktop can attach a scrollbar and mobile can
 * reserve bottom-bar space.
 *
 * With no books yet it renders the same layout from the state's loading stand-ins.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingBooksColumn(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    controller: MarkAsReadController,
    listState: LazyListState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
) {
    val featured = state.featuredBook
    val isLoading = state.isLoadingFeatured

    val animator = rememberLazyItemMutationAnimator(
        keys = remember(state.alsoReading) {
            state.alsoReading.cards.filterNot(BookCardUiModel::isLoading).map { it.key.lazyKey }
        },
    )

    val entry = rememberStaggeredEntryCoordinator(key = "reading:rest")

    val isInspection = LocalInspectionMode.current
    val prefetcher = if (isInspection) null else rememberBookDetailPrefetcher()

    CompositionLocalProvider(LocalBookDetailPrefetcher provides prefetcher) {
        val onCardEvent = rememberPrefetchingBookCardEventHandler(
            set = state.alsoReading,
        ) { event, book ->
            when (event) {
                is BookCardEvent.Clicked -> onBookClick(book)

                is BookCardEvent.TrailingClicked -> runAction(OnShowProgressSheetClickAction(book = book))

                is BookCardEvent.ShakeEnded -> runAction(OnClearMutationFailureAction(bookId = book.id))

                is BookCardEvent.LongClicked,
                is BookCardEvent.PressStarted,
                is BookCardEvent.MenuClicked,
                    -> Unit
            }
        }

        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            item(key = "header") {
                header()
            }

            item(key = "featured-${featured.id}") {
                FeaturedBookCard(
                    book = featured,
                    backdropCover = state.featuredBackdropCover,
                    heroCover = state.featuredCover,
                    progress = state.featuredProgress,
                    deadlineCoverOverlay = state.deadlineCoverOverlays[featured.id],
                    deadlineSummary = state.featuredDeadlineSummary,
                    mutationFailed = featured.id in state.failedMutationBookIds,
                    paceForecast = state.featuredBookPace,
                    paceNudge = state.featuredPaceNudge,
                    onDismissPlanToday = {
                        runAction(OnDismissPlanTodayAction(bookId = featured.id))
                    },
                    runAction = runAction,
                    onBookClick = onBookClick,
                    modifier = controller.slideModifier(featured.id),
                    isLoading = isLoading,
                )
            }

            if (state.alsoReading.isEmpty.not()) {
                item(key = "also-reading-label") {
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        SectionHeader(model = state.alsoReadingHeader)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(
                    state.alsoReading.cards,
                    key = { _, card -> card.key.lazyKey },
                ) { index, card ->
                    val bookId = (card.key as? BookCardKey.Book)?.bookId ?: return@itemsIndexed

                    BookCard(
                        model = card,
                        onEvent = onCardEvent,
                        modifier = Modifier.mutationAnimated(
                            scope = this,
                            animator = animator,
                            itemKey = card.key.lazyKey,
                        )
                            .let {
                                if (card.isLoading) {
                                    it
                                } else {
                                    it.staggeredEntry(
                                        coordinator = entry,
                                        index = index,
                                    )
                                }
                            }
                            .then(controller.slideModifier(bookId)),
                    )
                }
            }
        }
    }
}
