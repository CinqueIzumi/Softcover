package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

@Composable
fun SheetScaffold(
    model: SheetScaffoldUiModel,
    onEvent: (SheetScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dismissStarted = remember { mutableStateOf(false) }
    val outsideDismissAllowed = model.dismissible && model.busy.not() && dismissStarted.value.not()

    AdaptiveModalSheet(
        onDismissRequest = { onEvent(SheetScaffoldEvent.Dismissed) },
        modifier = modifier,
        dismissOnTapOutside = outsideDismissAllowed,
        dismissOnBackPress = outsideDismissAllowed,
    ) {
        SheetScaffoldContent(
            model = model,
            onEvent = onEvent,
            dismissStarted = dismissStarted,
            content = content,
        )
    }
}

/** The scaffold's regions without the modal container, so the gallery and previews can host them inline. */
@Composable
internal fun SheetScaffoldContent(
    model: SheetScaffoldUiModel,
    onEvent: (SheetScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    dismissStarted: MutableState<Boolean> = remember { mutableStateOf(false) },
    content: @Composable ColumnScope.() -> Unit,
) {
    val chrome = rememberSheetChrome(
        model = model,
        dismissStarted = dismissStarted,
    )

    LaunchedEffect(model.closeRequested) {
        if (model.closeRequested) {
            chrome.close()
        }
    }

    val scrollState = rememberScrollState()
    val paneTitle = model.header?.let { header -> header.section.headlineText ?: header.section.eyebrow }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .then(if (model.layout == SheetLayout.Scrolling) Modifier.verticalScroll(state = scrollState) else Modifier)
            .padding(horizontal = Spacing.gutter)
            .padding(
                top = Spacing.xs,
                bottom = Spacing.md,
            )
            .semantics {
                if (paneTitle != null) {
                    this.paneTitle = paneTitle
                }
            },
    ) {
        SheetTop(
            model = model,
            chrome = chrome,
            onEvent = onEvent,
        )

        CompositionLocalProvider(LocalModalSheetDismiss provides chrome::dismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (model.layout != SheetLayout.Scrolling) {
                            Modifier.weight(
                                weight = 1f,
                                fill = false,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .then(if (model.layout == SheetLayout.Pinned) Modifier.verticalScroll(state = scrollState) else Modifier)
                    .blockInput(enabled = chrome.unlocked.not()),
                content = content,
            )
        }

        if (model.footer != null) {
            if (model.layout != SheetLayout.Scrolling) {
                Spacer(modifier = Modifier.height(Spacing.md))

                Divider()

                Spacer(modifier = Modifier.height(Spacing.sm))
            } else {
                Spacer(modifier = Modifier.height(Spacing.xl))
            }

            SheetFooter(
                model = model.footer,
                onEvent = onEvent,
                chrome = chrome,
            )
        }
    }
}

private fun Modifier.blockInput(enabled: Boolean): Modifier = if (enabled) {
    onPreviewKeyEvent { true }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitPointerEvent(PointerEventPass.Initial).changes.forEach(PointerInputChange::consume)
                }
            }
        }
} else {
    this
}

@Composable
private fun SheetTop(
    model: SheetScaffoldUiModel,
    chrome: SheetChrome,
    onEvent: (SheetScaffoldEvent) -> Unit,
) {
    if (model.dismissLabel != null) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            RhaydusButton(
                label = model.dismissLabel,
                style = ButtonStyle.TEXT,
                onClick = chrome::dismiss,
                enabled = chrome.canDismiss && chrome.unlocked,
            )
        }
    }

    if (model.header != null) {
        SheetHeader(
            model = model.header,
            chrome = chrome,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

@Composable
private fun SheetHeader(
    model: SheetHeaderUiModel,
    chrome: SheetChrome,
    onEvent: (SheetScaffoldEvent) -> Unit,
) {
    val trailing = model.trailing

    SectionHeader(
        model = model.section,
        trailing = if (trailing == null) {
            null
        } else {
            {
                Spacer(modifier = Modifier.width(Spacing.xs))

                SheetHeaderTrailingContent(
                    trailing = trailing,
                    chrome = chrome,
                    onEvent = onEvent,
                )
            }
        },
    )
}

@Composable
private fun RowScope.SheetHeaderTrailingContent(
    trailing: SheetHeaderTrailing,
    chrome: SheetChrome,
    onEvent: (SheetScaffoldEvent) -> Unit,
) {
    when (trailing) {
        is SheetHeaderTrailing.Jacket -> Cover(
            model = trailing.cover,
            modifier = Modifier.width(56.dp),
        )

        is SheetHeaderTrailing.JacketStack -> JacketStack(trailing = trailing)

        is SheetHeaderTrailing.Action -> RhaydusButton(
            label = trailing.label,
            style = ButtonStyle.TEXT,
            onClick = {
                if (chrome.unlocked && chrome.busy.not()) {
                    onEvent(SheetScaffoldEvent.HeaderActionClicked)

                    if (trailing.dismissesSheet) {
                        chrome.dismiss()
                    }
                }
            },
            enabled = chrome.unlocked && chrome.busy.not(),
        )
    }
}

@Composable
private fun JacketStack(trailing: SheetHeaderTrailing.JacketStack) {
    Box(
        modifier = Modifier.size(
            width = 120.dp,
            height = 96.dp,
        ),
        contentAlignment = Alignment.Center,
    ) {
        trailing.covers.indices.reversed().forEach { index ->
            val side = JACKET_FAN[index]

            Cover(
                model = trailing.covers[index],
                modifier = Modifier
                    .offset(x = Spacing.xxl * side)
                    .width(52.dp)
                    .rotate(10f * side),
            )
        }
    }
}

/** Fan side per jacket index: the first upright in front, then one behind on each side. */
private val JACKET_FAN = listOf(0, -1, 1)

@Composable
private fun SheetFooter(
    model: SheetFooterUiModel,
    onEvent: (SheetScaffoldEvent) -> Unit,
    chrome: SheetChrome,
) {
    when (model) {
        is SheetFooterUiModel.Stacked -> Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            if (model.primary != null) {
                FooterButton(
                    button = model.primary,
                    primary = true,
                    fillWidth = true,
                    chrome = chrome,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (model.secondary != null) {
                FooterButton(
                    button = model.secondary,
                    primary = false,
                    fillWidth = true,
                    chrome = chrome,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        is SheetFooterUiModel.Inline -> Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            FooterButton(
                button = model.secondary,
                primary = false,
                fillWidth = false,
                chrome = chrome,
                onEvent = onEvent,
            )

            FooterButton(
                button = model.primary,
                primary = true,
                fillWidth = true,
                chrome = chrome,
                onEvent = onEvent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FooterButton(
    button: SheetFooterButton,
    primary: Boolean,
    fillWidth: Boolean,
    chrome: SheetChrome,
    onEvent: (SheetScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelAlpha by animateFloatAsState(
        targetValue = if (button.running && fillWidth.not()) 0f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
    )

    Box(
        modifier = modifier.then(
            if (button.running) {
                Modifier.semantics { stateDescription = "Loading" }
            } else {
                Modifier
            },
        ),
        contentAlignment = if (fillWidth) Alignment.CenterEnd else Alignment.Center,
    ) {
        RhaydusButton(
            label = button.label,
            style = when {
                button.emphasis == SheetFooterEmphasis.Outlined -> ButtonStyle.OUTLINED
                primary -> ButtonStyle.FILLED
                else -> ButtonStyle.TEXT
            },
            icon = button.icon?.let { icon ->
                drawableIconResource(
                    icon = icon,
                    contentDescription = "",
                )
            },
            onClick = {
                if (chrome.unlocked) {
                    onEvent(if (primary) SheetScaffoldEvent.PrimaryClicked else SheetScaffoldEvent.SecondaryClicked)

                    if (button.dismissesSheet) {
                        chrome.dismiss()
                    }
                }
            },
            modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier.alpha(labelAlpha),
            enabled = button.enabled && chrome.busy.not() && chrome.unlocked,
            size = ButtonSize.M,
        )

        AnimatedVisibility(
            visible = button.running,
            enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
        ) {
            CircularWavyProgressIndicator(
                modifier = Modifier
                    .padding(end = if (fillWidth) Spacing.md else 0.dp)
                    .size(20.dp),
            )
        }
    }
}
