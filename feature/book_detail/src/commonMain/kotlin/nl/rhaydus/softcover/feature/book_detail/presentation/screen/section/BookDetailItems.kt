package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnLensSelectedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

internal fun LazyListScope.lensItems(
    state: BookDetailUiState,
    similarBooksListState: LazyListState,
    runAction: (BookDetailAction) -> Unit,
) {
    stickyHeader(key = "lens_toggle") {
        LensToggle(
            model = state.lensToggle,
            onLensSelected = { lens -> runAction(OnLensSelectedAction(lens = lens)) },
        )
    }

    item(key = "lens_spacer") { Spacer(modifier = Modifier.height(Spacing.xl)) }

    item(key = "lens_content") {
        LensContent(
            state = state,
            similarBooksListState = similarBooksListState,
            runAction = runAction,
        )
    }
}

internal fun LazyListScope.bookDetailItems(
    state: BookDetailUiState,
    similarBooksListState: LazyListState,
    runAction: (BookDetailAction) -> Unit,
    onCoverClick: () -> Unit,
) {
    item(key = "hero") {
        BookHero(
            state = state,
            runAction = runAction,
            onCoverClick = onCoverClick,
        )
    }

    item(key = "hero_spacer") { Spacer(modifier = Modifier.height(Spacing.xxl)) }

    item(key = "shelve_control") {
        ShelveControlCard(
            state = state,
            runAction = runAction,
        )
    }

    state.scanEditionCallout?.let { scanEditionCallout ->
        item(key = "scan_callout_spacer") { Spacer(modifier = Modifier.height(Spacing.xxl)) }

        item(key = "scan_callout") {
            ScanEditionCallout(
                model = scanEditionCallout,
                runAction = runAction,
            )
        }
    }

    item(key = "lens_spacer_top") { Spacer(modifier = Modifier.height(Spacing.xxl)) }

    lensItems(
        state = state,
        similarBooksListState = similarBooksListState,
        runAction = runAction,
    )
}
