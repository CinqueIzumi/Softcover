package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners

private const val MULTILINE_MIN_LINES = 4

@Composable
internal fun MultilineTextFieldBody(
    model: TextFieldUiModel,
    onEvent: (TextFieldEvent) -> Unit,
    focusRequester: FocusRequester,
) {
    OutlinedTextField(
        value = model.text,
        onValueChange = model.onTextChange(onEvent),
        placeholder = {
            TextFieldPlaceholder(
                text = model.placeholder,
                enabled = model.enabled,
            )
        },
        enabled = model.enabled,
        isError = model.error != null,
        minLines = MULTILINE_MIN_LINES,
        shape = Corners.sm,
        keyboardOptions = model.keyboardOptions(),
        colors = OutlinedTextFieldDefaults.colors().copy(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics { contentDescription = model.placeholder },
    )
}
