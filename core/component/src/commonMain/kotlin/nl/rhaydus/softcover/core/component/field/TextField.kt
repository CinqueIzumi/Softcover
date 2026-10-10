package nl.rhaydus.softcover.core.component.field

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

@Composable
fun TextField(
    model: TextFieldUiModel,
    onEvent: (TextFieldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val centred = model.variant is TextFieldVariant.Hero || model.variant is TextFieldVariant.Numeral

    // The foundation shake cannot be cancelled cleanly, so latch it: a mid-shake edit clears the
    // error but the shake still completes and recentres.
    var shaking by remember { mutableStateOf(false) }

    LaunchedEffect(model.error?.shaking) {
        if (model.error?.shaking == true) shaking = true
    }

    LaunchedEffect(model.autoFocus) {
        if (model.autoFocus) focusRequester.requestFocus()
    }

    Column(
        horizontalAlignment = if (centred) Alignment.CenterHorizontally else Alignment.Start,
        modifier = modifier
            .shakeOnError(
                trigger = shaking,
                onShakeEnd = {
                    shaking = false
                    onEvent(TextFieldEvent.ShakeEnded(key = model.key))
                },
            )
            .semantics { model.error?.let { error(it.message) } },
    ) {
        when (val variant = model.variant) {
            is TextFieldVariant.Pill -> PillTextField(
                model = model,
                variant = variant,
                onEvent = onEvent,
                focusRequester = focusRequester,
            )

            is TextFieldVariant.Outlined -> OutlinedTextFieldBody(
                model = model,
                variant = variant,
                onEvent = onEvent,
                focusRequester = focusRequester,
            )

            is TextFieldVariant.Hero -> HeroTextField(
                model = model,
                onEvent = onEvent,
                focusRequester = focusRequester,
            )

            is TextFieldVariant.Numeral -> NumeralTextField(
                model = model,
                variant = variant,
                onEvent = onEvent,
                focusRequester = focusRequester,
            )
        }

        val motionScheme = MaterialTheme.motionScheme

        AnimatedContent(
            targetState = model.error?.message,
            transitionSpec = {
                fadeIn(animationSpec = motionScheme.defaultEffectsSpec())
                    .togetherWith(fadeOut(animationSpec = motionScheme.defaultEffectsSpec()))
                    .using(SizeTransform { _, _ -> motionScheme.defaultSpatialSpec() })
            },
        ) { message ->
            if (message != null) {
                TextFieldErrorLine(
                    message = message,
                    centred = centred,
                )
            }
        }
    }
}

@Composable
private fun TextFieldErrorLine(
    message: String,
    centred: Boolean,
) {
    SoftcoverText(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        textAlign = if (centred) TextAlign.Center else TextAlign.Start,
        modifier = Modifier
            .padding(top = Spacing.xs)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}
