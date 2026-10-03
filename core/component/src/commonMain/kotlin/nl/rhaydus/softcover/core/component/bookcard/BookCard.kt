package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.modifier.pressScaleCombinedClickable
import nl.rhaydus.designsystem.modifier.shakeOnError

/**
 * [content] is positioned but not sized by the card: at the cover's top-end corner on cover-only and
 * column cards, at the row's end on rows.
 */
@Composable
fun BookCard(
    model: BookCardUiModel,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null,
) {
    when (val variant = model.variant) {
        is BookCardVariant.CoverOnly -> BookCardCoverOnlyLayout(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
            isLoading = model.isLoading,
            content = content,
        )

        is BookCardVariant.Column -> BookCardColumnLayout(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
            isLoading = model.isLoading,
            content = content,
        )

        is BookCardVariant.Row -> BookCardRowLayout(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
            isLoading = model.isLoading,
            content = content,
        )
    }
}

/**
 * [BookCardEvent.PressStarted] fires only for downs no child has consumed, so a tap on a trailing control,
 * menu or [content] never reads as a card press.
 */
@Composable
internal fun Modifier.bookCardInteraction(
    model: BookCardUiModel,
    onEvent: (BookCardEvent) -> Unit,
    hoverHighlight: Boolean,
    isLoading: Boolean,
): Modifier {
    if (isLoading) return this

    val decorations = model.decorations
    val key = model.key
    val latestOnEvent by rememberUpdatedState(newValue = onEvent)
    val interactive = decorations.pressBehaviour != BookCardDecorations.Press.None
    val selection = decorations.selection
    val hoverSource = remember { MutableInteractionSource() }

    val hover = if (hoverHighlight && interactive) {
        Modifier
            .hoverable(interactionSource = hoverSource)
            .hoverHighlight(interactionSource = hoverSource)
    } else {
        Modifier
    }

    val press = when (decorations.pressBehaviour) {
        BookCardDecorations.Press.Tap -> Modifier.pressScaleClickable(
            onClick = { latestOnEvent(BookCardEvent.Clicked(key = key)) },
        )

        BookCardDecorations.Press.TapAndLongPress -> Modifier.pressScaleCombinedClickable(
            onClick = { latestOnEvent(BookCardEvent.Clicked(key = key)) },
            onLongClick = { latestOnEvent(BookCardEvent.LongClicked(key = key)) },
        )

        BookCardDecorations.Press.None -> Modifier
    }

    val pressStarted = if (interactive) {
        Modifier
            .pointerHandCursor()
            .pointerInput(key) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = true)
                    latestOnEvent(BookCardEvent.PressStarted(key = key))
                }
            }
    } else {
        Modifier
    }

    return this
        .shakeOnError(
            trigger = decorations.shakeOnError,
            onShakeEnd = { latestOnEvent(BookCardEvent.ShakeEnded(key = key)) },
        )
        .then(hover)
        .then(press)
        .then(pressStarted)
        .semantics {
            val semantics = bookCardSemantics(
                interactive = interactive,
                selection = selection,
            )

            semantics.role?.let { role = it }
            semantics.selected?.let { selected = it }
            semantics.toggleableState?.let { toggleableState = it }
        }
}

internal data class BookCardSemantics(
    val role: Role?,
    val selected: Boolean?,
    val toggleableState: ToggleableState?,
)

internal fun bookCardSemantics(
    interactive: Boolean,
    selection: BookCardSelection?,
): BookCardSemantics = when {
    interactive && selection is BookCardSelection.Multiple -> BookCardSemantics(
        role = Role.Checkbox,
        selected = null,
        toggleableState = ToggleableState(selection.selected),
    )

    interactive && selection is BookCardSelection.Single -> BookCardSemantics(
        role = Role.RadioButton,
        selected = selection.selected,
        toggleableState = null,
    )

    interactive -> BookCardSemantics(
        role = Role.Button,
        selected = null,
        toggleableState = null,
    )

    selection != null -> BookCardSemantics(
        role = null,
        selected = selection.selected,
        toggleableState = null,
    )

    else -> BookCardSemantics(
        role = null,
        selected = null,
        toggleableState = null,
    )
}
