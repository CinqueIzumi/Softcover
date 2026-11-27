package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.feeling.HowItFeltBlock
import nl.rhaydus.softcover.core.component.feeling.HowItFeltBlockEvent
import nl.rhaydus.softcover.core.component.feeling.HowItFeltBlockUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMoodMomentClickedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenHowItFeltSheetAction

@Composable
internal fun HowItFeltSection(
    model: HowItFeltBlockUiModel,
    runAction: (BookDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    HowItFeltBlock(
        model = model,
        onEvent = { event ->
            when (event) {
                HowItFeltBlockEvent.EditRequested -> runAction(OnOpenHowItFeltSheetAction())

                is HowItFeltBlockEvent.MomentClicked -> runAction(OnMoodMomentClickedAction(id = event.id))
            }
        },
        modifier = modifier.padding(horizontal = Spacing.gutter),
    )
}
