package nl.rhaydus.softcover.core.component.header

import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.header.MastheadRefreshEvent.Refresh
import nl.rhaydus.softcover.core.component.text.SoftcoverText

/** A [PageMasthead] trailing action: refreshes the page it names. */
@Composable
fun MastheadRefreshAction(
    model: MastheadRefreshUiModel,
    onEvent: (MastheadRefreshEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = { onEvent(Refresh) },
        enabled = model.enabled,
        modifier = modifier.pointerHandCursor(),
    ) {
        SoftcoverText(text = model.label)
    }
}
