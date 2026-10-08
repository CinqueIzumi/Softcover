package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.InlineErrorState
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
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
            TextField(
                model = model.apiKeyField,
                onEvent = { event ->
                    when (event) {
                        is TextFieldEvent.ValueChanged -> onEvent(ReAuthSheetEvent.ApiKeyChanged(event.text))

                        is TextFieldEvent.Submitted,
                        is TextFieldEvent.TrailingClicked -> Unit
                    }
                },
            )

            if (model.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))

                InlineErrorState(
                    message = model.errorMessage,
                    onRetry = { onEvent(ReAuthSheetEvent.Submitted) },
                    textStyle = MaterialTheme.editorialTypography.bodySmall,
                )
            }
        }
    }
}
