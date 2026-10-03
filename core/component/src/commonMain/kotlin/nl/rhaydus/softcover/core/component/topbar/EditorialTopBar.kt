package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The desktop page bar: a back button, a start-aligned editorial headline, and a trailing slot for
 * the screen's own actions.
 */
@Composable
fun EditorialTopBar(
    model: EditorialTopBarUiModel,
    onEvent: (EditorialTopBarEvent) -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 8.dp,
                top = 12.dp,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChromeIconButton(
            icon = SoftcoverIcon.ArrowBack,
            contentDescription = "Navigate back",
            tooltip = "Back",
            onClick = { onEvent(EditorialTopBarEvent.BackClicked) },
        )

        model.title?.let { title ->
            Spacer(modifier = Modifier.width(4.dp))

            SoftcoverText(
                text = title,
                style = MaterialTheme.editorialTypography.headlineMedium,
                modifier = Modifier.semantics { heading() },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        actions()
    }
}
