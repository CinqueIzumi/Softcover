package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun ReAuthSheet(
    model: ReAuthSheetUiModel,
    onEvent: (ReAuthSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetScaffold(
        model = model.scaffold,
        modifier = modifier,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onEvent(ReAuthSheetEvent.Submitted)

                SheetScaffoldEvent.SecondaryClicked -> onEvent(ReAuthSheetEvent.LoggedOut)

                SheetScaffoldEvent.Dismissed -> onEvent(ReAuthSheetEvent.Dismissed)

                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        Column {
            OutlinedTextField(
                value = model.apiKey,
                onValueChange = { onEvent(ReAuthSheetEvent.ApiKeyChanged(it)) },
                label = { Text(text = "API token") },
                placeholder = { Text(text = "Paste your Hardcover API token") },
                enabled = model.fieldEnabled,
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors().copy(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            if (model.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = model.errorMessage,
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
