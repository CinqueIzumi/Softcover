package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.callout.Callout
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.bookDetailActionFor

@Composable
internal fun ScanEditionCallout(
    model: CalloutUiModel,
    runAction: (BookDetailAction) -> Unit,
) {
    Callout(
        model = model,
        onEvent = { event -> runAction(bookDetailActionFor(event)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter),
    )
}
