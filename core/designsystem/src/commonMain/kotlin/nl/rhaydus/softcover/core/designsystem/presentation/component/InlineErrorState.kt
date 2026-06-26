package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * Inline error state: renders a [message] in the error color role and a "Retry" affordance that
 * calls [onRetry]. Intended as the standard surface for a TOAD `String?` error-slot — drop it
 * where a load or submit failure should surface instead of the normal content.
 *
 * The caller owns sizing and outer padding via [modifier]. Content is always centered horizontally;
 * vertical centering is opt-in — it only takes effect when the [modifier] gives the column a height
 * (e.g. `Modifier.fillMaxSize()` for a full results pane). With no height the column wraps its content,
 * so it simply stacks inline below the preceding element (e.g. under a form's submit button).
 */
@Composable
fun InlineErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        RhaydusButton(
            label = "Retry",
            onClick = onRetry,
            style = ButtonStyle.OUTLINED,
            size = ButtonSize.S,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
