package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.traits.HowItReadBlock
import nl.rhaydus.softcover.core.component.traits.HowItReadBlockEvent
import nl.rhaydus.softcover.core.component.traits.HowItReadBlockUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnHowItReadToggledAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenHowItReadSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

@Composable
internal fun HowItReadSection(
    state: BookDetailUiState,
    model: HowItReadBlockUiModel,
    runAction: (BookDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = Spacing.gutter)) {
        SectionHeader(model = state.howItReadLabel)

        Spacer(modifier = Modifier.height(Spacing.md))

        HowItReadBlock(
            model = model,
            onEvent = { event ->
                when (event) {
                    HowItReadBlockEvent.ExpandToggled -> runAction(OnHowItReadToggledAction())

                    HowItReadBlockEvent.EditRequested -> runAction(OnOpenHowItReadSheetAction())
                }
            },
        )
    }
}
