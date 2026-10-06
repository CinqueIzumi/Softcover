package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnLensSelectedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

internal fun LazyListScope.lensItems(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    stickyHeader {
        LensToggle(
            model = state.lensToggle,
            onLensSelected = { lens -> runAction(OnLensSelectedAction(lens = lens)) },
        )
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }

    item {
        LensContent(
            state = state,
            runAction = runAction,
        )
    }
}

internal fun LazyListScope.bookDetailItems(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    celebrationKey: Int,
    onCoverClick: () -> Unit,
) {
    item {
        BookHero(
            state = state,
            runAction = runAction,
            onCoverClick = onCoverClick,
        )
    }

    item { Spacer(modifier = Modifier.height(20.dp)) }

    item {
        ShelveControlCard(
            state = state,
            runAction = runAction,
            dateStyle = state.dateStyle,
            celebrationKey = celebrationKey,
        )
    }

    state.scanEditionCallout?.let { scanEditionCallout ->
        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            ScanEditionCallout(
                model = scanEditionCallout,
                runAction = runAction,
            )
        }
    }

    item { Spacer(modifier = Modifier.height(20.dp)) }

    lensItems(
        state = state,
        runAction = runAction,
    )
}
