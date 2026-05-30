package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography

/**
 * Editorial editing surface for the user's personal review. The body field uses the italic editorial
 * body face so writing here reads like a journal entry rather than a form. "Save" publishes the text
 * to Hardcover (offline-replayable); the review is owned by Hardcover, so it round-trips and is
 * overwritten on the next refresh. "Delete" clears it and is only offered once a review exists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewEditorBottomSheet(
    body: String,
    hasSpoilers: Boolean,
    canDelete: Boolean,
    onBodyChange: (String) -> Unit,
    onSpoilerToggle: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = "Your review",
                headline = "Write a few words.",
                description = "Saved to Hardcover and shown here — editing it elsewhere updates this too.",
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = body,
                onValueChange = onBodyChange,
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text(text = "What stayed with you?") },
                textStyle = MaterialTheme.editorialTypography.body,
                colors = OutlinedTextFieldDefaults.colors().copy(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp)
                    .focusRequester(focusRequester),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Contains spoilers",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Readers will need to tap to reveal it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = hasSpoilers,
                    onCheckedChange = onSpoilerToggle,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SoftcoverButton(
                label = "Save",
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                enabled = body.isBlank().not(),
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
            )

            if (canDelete) {
                Spacer(modifier = Modifier.height(8.dp))

                SoftcoverButton(
                    label = "Delete review",
                    style = ButtonStyle.TEXT,
                    size = ButtonSize.M,
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
