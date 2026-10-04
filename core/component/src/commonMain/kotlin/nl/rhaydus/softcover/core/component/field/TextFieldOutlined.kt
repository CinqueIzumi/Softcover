package nl.rhaydus.softcover.core.component.field

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.text.SoftcoverText

@Composable
internal fun OutlinedTextFieldBody(
    model: TextFieldUiModel,
    variant: TextFieldVariant.Outlined,
    onEvent: (TextFieldEvent) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = model.text,
        onValueChange = model.onTextChange(onEvent),
        label = variant.label?.let { label ->
            { SoftcoverText(text = label) }
        },
        placeholder = {
            TextFieldPlaceholder(
                text = model.placeholder,
                enabled = model.enabled,
            )
        },
        enabled = model.enabled,
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = model.keyboardOptions(),
        keyboardActions = model.keyboardActions(onEvent),
        colors = OutlinedTextFieldDefaults.colors().copy(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .then(
                if (variant.label == null) {
                    Modifier.semantics { contentDescription = model.placeholder }
                } else {
                    Modifier
                },
            ),
    )
}
