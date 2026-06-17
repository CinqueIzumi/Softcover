package nl.rhaydus.softcover.feature.lists.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.feature.lists.presentation.action.CreateListAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnNameChangedAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnSubmitAction
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState

/**
 * The "name your list" form, shared verbatim by the mobile Scaffold layout and the desktop modal
 * panel. It auto-focuses the field on first composition and submits on the IME Done action (Enter on
 * desktop) when the name is valid. Outer padding is the caller's responsibility — mobile sets the page
 * margins, desktop the panel insets. Hover/cursor on the submit button is inert on touch, so mobile
 * renders identically.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateListForm(
    state: CreateListUiState,
    runAction: (CreateListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        EditorialSectionHeader(
            eyebrow = "Custom list",
            headline = "Name your list.",
            description = "Pick a short label — you can rename it later.",
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { runAction(OnNameChangedAction(newName = it)) },
            shape = RoundedCornerShape(8.dp),
            placeholder = { Text(text = "e.g. Summer reads") },
            enabled = state.isSubmitting.not(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (state.canSubmit) {
                        keyboardController?.hide()

                        runAction(OnSubmitAction())
                    }
                },
            ),
            colors = OutlinedTextFieldDefaults.colors().copy(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )

        Spacer(modifier = Modifier.height(24.dp))

        SoftcoverButton(
            label = if (state.isSubmitting) "Creating" else "Create list",
            style = ButtonStyle.FILLED,
            size = ButtonSize.M,
            enabled = state.canSubmit,
            onClick = {
                keyboardController?.hide()

                runAction(OnSubmitAction())
            },
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor(),
        )
    }
}
