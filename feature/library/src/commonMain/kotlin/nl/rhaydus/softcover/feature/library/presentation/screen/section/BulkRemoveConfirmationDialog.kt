package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun BulkRemoveConfirmationDialog(
    bookCount: Int,
    inProgress: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val titlePlural = if (bookCount == 1) "book" else "books"

    // A transient destructive confirm is a bare overlay (§6) rather than a sheet, but it still
    // carries the editorial register — Fraunces headline + body and the shared button — instead of
    // Material AlertDialog chrome.
    Dialog(
        onDismissRequest = {
            if (inProgress.not()) onDismiss()
        },
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Remove $bookCount $titlePlural?",
                    style = MaterialTheme.editorialTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "They'll come off every shelf and out of your Hardcover library. " +
                        "You can always add them again later.",
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.End,
                    ),
                ) {
                    RhaydusButton(
                        label = "Keep",
                        style = ButtonStyle.TEXT,
                        onClick = onDismiss,
                        enabled = inProgress.not(),
                    )

                    RhaydusButton(
                        label = "Remove",
                        style = ButtonStyle.TEXT,
                        onClick = onConfirm,
                        enabled = inProgress.not(),
                    )
                }
            }
        }
    }
}
