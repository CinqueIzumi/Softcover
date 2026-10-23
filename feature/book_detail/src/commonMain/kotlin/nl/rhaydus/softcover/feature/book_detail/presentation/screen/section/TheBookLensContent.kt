package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * The "The Book" lens content (design-system.md's lens-toggle pattern): the book's own facts, in
 * order — About, Tags (community tags + edition colophon), Similar books, Find it, Voices. Sections sit
 * a `Spacing.section` apart.
 */
@Composable
internal fun TheBookLensContent(
    state: BookDetailUiState,
    similarBooksListState: LazyListState,
    runAction: (BookDetailAction) -> Unit,
) {
    // Each section below (Tags, Similar books, Find it, Voices) supplies its own leading section gap, guarded on the
    // same early-return that decides whether it renders anything — so a book missing one of them
    // (e.g. no ISBN, no community tags or colophon) never leaves a dead gap between its neighbors.
    Column(modifier = Modifier.fillMaxWidth()) {
        AboutSection(state = state)

        TagsSection(
            state = state,
            runAction = runAction,
        )

        SimilarBooksSection(
            state = state,
            listState = similarBooksListState,
            runAction = runAction,
        )

        ExternalLinksSection(
            state = state,
            runAction = runAction,
        )

        ReviewsSection(
            state = state,
            runAction = runAction,
        )
    }
}
