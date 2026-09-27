package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenTagEditorAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UserTagsSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val chips = state.userTagChips
    val opener = state.tagEditorOpenerChip
    val onOpenerEvent: (ChipEvent) -> Unit = { event ->
        if (event is ChipEvent.Clicked) runAction(OnOpenTagEditorAction())
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SmallSectionLabel(text = "Your tags")

        Spacer(modifier = Modifier.height(16.dp))

        if (chips.isEmpty()) {
            opener?.let {
                Chip(
                    model = it,
                    onEvent = onOpenerEvent,
                )
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                chips.forEach { chip ->
                    key(chip.key) {
                        Chip(model = chip)
                    }
                }

                opener?.let {
                    key(it.key) {
                        Chip(
                            model = it,
                            onEvent = onOpenerEvent,
                        )
                    }
                }
            }
        }
    }
}
