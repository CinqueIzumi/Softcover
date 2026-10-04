package nl.rhaydus.softcover.feature.scan.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText

/**
 * The manual ISBN-entry form, shared by the mobile scanner (reached via "Enter ISBN manually") and the
 * desktop scanner (the only path there — desktop has no camera). Submits on the IME Done action (Enter
 * on desktop) when an ISBN is present and nothing is in flight. Outer padding is the caller's; [autoFocus]
 * asks the host, through [onAutoFocusRequested], to set the field model's `autoFocus` (desktop opens
 * straight into the form, so it asks; mobile defaults to off, preserving today's behaviour).
 * Hover/cursor on the submit button is inert on touch.
 */
@Composable
internal fun ManualEntryContent(
    field: TextFieldUiModel,
    isResolving: Boolean,
    isAddingBook: Boolean,
    header: SectionHeaderUiModel,
    onFieldEvent: (TextFieldEvent) -> Unit,
    onIsbnSubmit: (String) -> Unit,
    autoFocus: Boolean,
    onAutoFocusRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val canSubmit = field.text.isNotBlank() && (isResolving || isAddingBook).not()

    val submit: () -> Unit = {
        if (canSubmit) {
            keyboardController?.hide()

            onIsbnSubmit(field.text)
        }
    }

    LaunchedEffect(Unit) {
        if (autoFocus) onAutoFocusRequested()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            model = field,
            onEvent = { event ->
                when (event) {
                    is TextFieldEvent.Submitted -> submit()

                    is TextFieldEvent.ValueChanged,
                    is TextFieldEvent.TrailingClicked -> onFieldEvent(event)
                }
            },
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
