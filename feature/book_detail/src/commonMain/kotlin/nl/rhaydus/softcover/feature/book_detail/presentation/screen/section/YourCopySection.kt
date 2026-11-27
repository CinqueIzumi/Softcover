package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.yourcopy.YourCopyPanel
import nl.rhaydus.softcover.core.component.yourcopy.YourCopyPanelEvent
import nl.rhaydus.softcover.core.component.yourcopy.YourCopyPanelUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenWhereYouGotItSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

@Composable
internal fun YourCopySection(
    state: BookDetailUiState,
    model: YourCopyPanelUiModel<String>,
    runAction: (BookDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = Spacing.gutter)) {
        SectionHeader(model = state.yourCopyLabel)

        Spacer(modifier = Modifier.height(Spacing.md))

        YourCopyPanel(
            model = model,
            onEvent = { event ->
                when (event) {
                    is YourCopyPanelEvent.RowClicked -> runAction(OnOpenWhereYouGotItSheetAction())
                }
            },
        )
    }
}
