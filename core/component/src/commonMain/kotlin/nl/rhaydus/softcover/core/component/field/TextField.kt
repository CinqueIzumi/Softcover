package nl.rhaydus.softcover.core.component.field

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester

@Composable
fun TextField(
    model: TextFieldUiModel,
    onEvent: (TextFieldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(model.autoFocus) {
        if (model.autoFocus) focusRequester.requestFocus()
    }

    when (val variant = model.variant) {
        is TextFieldVariant.Pill -> PillTextField(
            model = model,
            variant = variant,
            onEvent = onEvent,
            focusRequester = focusRequester,
            modifier = modifier,
        )

        is TextFieldVariant.Outlined -> OutlinedTextFieldBody(
            model = model,
            variant = variant,
            onEvent = onEvent,
            focusRequester = focusRequester,
            modifier = modifier,
        )

        TextFieldVariant.Hero -> HeroTextField(
            model = model,
            onEvent = onEvent,
            focusRequester = focusRequester,
            modifier = modifier,
        )

        is TextFieldVariant.Numeral -> NumeralTextField(
            model = model,
            variant = variant,
            onEvent = onEvent,
            focusRequester = focusRequester,
            modifier = modifier,
        )
    }
}
