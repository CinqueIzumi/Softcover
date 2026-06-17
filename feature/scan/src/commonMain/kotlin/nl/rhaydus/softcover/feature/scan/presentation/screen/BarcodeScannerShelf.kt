package nl.rhaydus.softcover.feature.scan.presentation.screen

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader

/**
 * The manual ISBN-entry form, shared by the mobile scanner (reached via "Enter ISBN manually") and the
 * desktop scanner (the only path there — desktop has no camera). Submits on the IME Done action (Enter
 * on desktop) when an ISBN is present and nothing is in flight. Outer padding is the caller's; [autoFocus]
 * focuses the field on first composition (desktop opens straight into the form, so it focuses; mobile
 * defaults to off, preserving today's behaviour). Hover/cursor on the submit button is inert on touch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManualEntryContent(
    isResolving: Boolean,
    isAddingBook: Boolean,
    onIsbnSubmit: (String) -> Unit,
    autoFocus: Boolean,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusRequester = remember { FocusRequester() }

    var isbn by remember { mutableStateOf("") }

    val busy = isResolving || isAddingBook

    val canSubmit = isbn.isNotBlank() && busy.not()

    val submit: () -> Unit = {
        if (canSubmit) {
            keyboardController?.hide()

            onIsbnSubmit(isbn)
        }
    }

    LaunchedEffect(Unit) {
        if (autoFocus) focusRequester.requestFocus()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        EditorialSectionHeader(
            eyebrow = "By ISBN",
            headline = "Enter the ISBN.",
            description = "Type the ISBN-10 or ISBN-13 printed near the barcode.",
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = isbn,
            onValueChange = { isbn = it },
            shape = RoundedCornerShape(8.dp),
            placeholder = { Text(text = "e.g. 9780374710781") },
            enabled = busy.not(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            colors = OutlinedTextFieldDefaults.colors().copy(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )

        Spacer(modifier = Modifier.height(24.dp))

        RhaydusButton(
            label = if (isResolving) "Looking up" else "Find book",
            style = ButtonStyle.FILLED,
            size = ButtonSize.M,
            enabled = canSubmit,
            onClick = submit,
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor(),
        )
    }
}
