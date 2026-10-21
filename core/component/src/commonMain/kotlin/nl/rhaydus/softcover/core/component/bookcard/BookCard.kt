package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.softcover.core.component.modifier.shakeOnError
import nl.rhaydus.softcover.core.component.modifier.tactileClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * [content] is positioned but not sized by the card: at the cover's top-end corner on cover-only and
 * column cards, at the row's end on rows. The card adds no inset and reserves no space for it, so a slot
 * that is empty (for example a handle that is currently hidden) leaves the card's layout untouched; the
 * slot owns its own padding.
 */
@Composable
fun BookCard(
    model: BookCardUiModel,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null,
) {
    when (val variant = model.variant) {
        is BookCardVariant.CoverOnly -> BookCardCoverScaffold(
            model = model,
            cover = variant.cover,
            hasMenu = false,
            text = null,
            bookmark = null,
            onEvent = onEvent,
            modifier = modifier,
            content = content,
        )

        is BookCardVariant.Column -> BookCardCoverScaffold(
            model = model,
            cover = variant.cover,
            hasMenu = variant.hasMenu,
            text = variant.text,
            bookmark = variant.bookmark,
            onEvent = onEvent,
            modifier = modifier,
            content = content,
        )

        is BookCardVariant.Row -> BookCardRowLayout(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
            content = content,
        )
    }
}

@Composable
private fun BookCardCoverScaffold(
    model: BookCardUiModel,
    cover: BookCardCover?,
    hasMenu: Boolean,
    text: BookCardContent?,
    bookmark: BookCardTrailing.Bookmark?,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier,
    content: (@Composable () -> Unit)?,
) {
    val progress = model.decorations.progress
    val isLoading = model.isLoading
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column(
        modifier = modifier.bookCardInteraction(
            model = model,
            onEvent = onEvent,
            interactionSource = interactionSource,
            hoverWash = false,
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            BookCardCoverLayout(
                cover = cover,
                selection = model.decorations.selection,
                lifted = isHovered,
                modifier = Modifier.fillMaxWidth(),
            )

            if (isLoading.not() && (hasMenu || bookmark != null || content != null)) {
                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    if (bookmark != null) {
                        val toggle = bookmark.toggle

                        BookCardCoverButton(
                            icon = if (toggle.checked) toggle.on else toggle.off,
                            contentDescription = toggle.description,
                            enabled = toggle.enabled,
                            checked = toggle.checked,
                            onClick = { onEvent(BookCardEvent.TrailingClicked(key = model.key)) },
                        )
                    }

                    if (hasMenu) {
                        BookCardCoverButton(
                            icon = SoftcoverIcon.MoreVert,
                            contentDescription = "More options",
                            onClick = { onEvent(BookCardEvent.MenuClicked(key = model.key)) },
                        )
                    }

                    if (content != null) {
                        content()
                    }
                }
            }
        }

        if (progress != null) {
            BookCardProgressBar(
                progress = progress,
                isLoading = isLoading,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }

        if (text != null) {
            BookCardTextBlock(
                content = text,
                titleStyle = MaterialTheme.editorialTypography.titleSmall,
                titleMinLines = 2,
                titleMaxLines = 2,
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xs),
            )
        }
    }
}

/**
 * The caller hoists [interactionSource] to read the card's own press and hover; [hoverWash] additionally paints
 * the hover highlight over the whole card.
 *
 * [BookCardEvent.PressStarted] fires only for downs no child has consumed, so a tap on a trailing control,
 * menu or [content] never reads as a card press.
 */
@Composable
internal fun Modifier.bookCardInteraction(
    model: BookCardUiModel,
    onEvent: (BookCardEvent) -> Unit,
    interactionSource: MutableInteractionSource,
    hoverWash: Boolean,
): Modifier {
    if (model.isLoading) return this

    val decorations = model.decorations
    val key = model.key
    val latestOnEvent by rememberUpdatedState(newValue = onEvent)
    val interactive = decorations.pressBehaviour != BookCardDecorations.Press.None
    val selection = decorations.selection

    val haptics = LocalHaptics.current
    val semantics = bookCardSemantics(
        interactive = interactive,
        selection = selection,
    )
    val press = if (interactive) {
        Modifier.tactileClickable(
            role = semantics.role,
            onClick = {
                if (selection is BookCardSelection.Multiple) haptics.select()
                latestOnEvent(BookCardEvent.Clicked(key = key))
            },
            onLongClick = if (decorations.pressBehaviour == BookCardDecorations.Press.TapAndLongPress) {
                { latestOnEvent(BookCardEvent.LongClicked(key = key)) }
            } else {
                null
            },
            hoverShape = if (hoverWash) RectangleShape else null,
            interactionSource = interactionSource,
        )
    } else {
        Modifier
    }

    val pressStarted = if (interactive) {
        Modifier.pointerInput(key) {
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
        .then(press)
        .then(pressStarted)
        .semantics {
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
