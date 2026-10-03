package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.mutationAnimated
import nl.rhaydus.designsystem.component.rememberLazyItemMutationAnimator
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.core.presentation.prefetch.rememberBookDetailPrefetcher
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
    val featured = state.books.first()
    val rest = state.books.drop(1)

    val animator = rememberLazyItemMutationAnimator(keys = rest.map { it.id })

    val entry = rememberStaggeredEntryCoordinator(key = "reading:rest")

    val isInspection = LocalInspectionMode.current
    val prefetcher = if (isInspection) null else rememberBookDetailPrefetcher()

    CompositionLocalProvider(LocalBookDetailPrefetcher provides prefetcher) {
        val onCardEvent = rememberReadingCardEventHandler(
            books = rest,
            onBookClick = onBookClick,
            onTrailingClick = { runAction(OnShowProgressSheetClickAction(book = it)) },
            onShakeEnd = { runAction(OnClearMutationFailureAction(bookId = it)) },
        )

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
                    deadlineCoverOverlay = state.deadlineCoverOverlays[featured.id],
                    deadlineSummary = state.featuredDeadlineSummary,
                    mutationFailed = featured.id in state.failedMutationBookIds,
                    paceForecast = state.featuredBookPace,
                    planTodayMessage = state.featuredPlanTodayMessage,
                    onDismissPlanToday = {
                        runAction(OnDismissPlanTodayAction(bookId = featured.id))
                    },
                    runAction = runAction,
                    onBookClick = onBookClick,
                    modifier = controller.slideModifier(featured.id),
                )
            }

            if (rest.isNotEmpty()) {
                item(key = "also-reading-label") {
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        SectionHeader(model = state.alsoReadingHeader)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(rest, key = { _, book -> book.id }) { index, book ->
                    state.alsoReadingCards[book.id]?.let { card ->
                        BookCard(
                            model = card,
                            onEvent = onCardEvent,
                            modifier = Modifier.mutationAnimated(
                                scope = this,
                                animator = animator,
                                itemKey = book.id,
                            )
                                .staggeredEntry(
                                    coordinator = entry,
                                    index = index,
                                )
                                .then(controller.slideModifier(book.id)),
                        )
                    }
                }
            }
        }
    }
}
