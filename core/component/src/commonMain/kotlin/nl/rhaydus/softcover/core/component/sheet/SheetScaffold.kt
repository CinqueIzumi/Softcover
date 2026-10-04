package nl.rhaydus.softcover.core.component.sheet

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.header.SectionHeader

@Composable
fun SheetScaffold(
    model: SheetScaffoldUiModel,
    onEvent: (SheetScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    var dismissStarted by remember { mutableStateOf(false) }
    val outsideDismissAllowed = model.dismissible && model.busy.not() && dismissStarted.not()

    AdaptiveModalSheet(
        onDismissRequest = { onEvent(SheetScaffoldEvent.Dismissed) },
        modifier = modifier,
        dismissOnTapOutside = outsideDismissAllowed,
        dismissOnBackPress = outsideDismissAllowed,
    ) {
        SheetScaffoldContent(
            model = model,
            onEvent = onEvent,
            onDismissStarted = { dismissStarted = true },
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
    onDismissStarted: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val chrome = rememberSheetChrome(model = model)
    val currentOnDismissStarted by rememberUpdatedState(onDismissStarted)

    LaunchedEffect(chrome.dismissal.started) {
        if (chrome.dismissal.started) {
            currentOnDismissStarted()
        }
    }

    LaunchedEffect(model.closeRequested) {
        if (model.closeRequested) {
            chrome.dismissal.start()
        }
    }

    val spec = model.layout.spec()
    val scrollState = rememberScrollState()
    val paneTitle = model.header.section.headlineText ?: model.header.section.eyebrow

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .then(if (spec.wholeSheetScrolls) Modifier.verticalScroll(state = scrollState) else Modifier)
            .padding(horizontal = 24.dp)
            .padding(
                top = 8.dp,
                bottom = 16.dp,
            )
            .semantics { this.paneTitle = paneTitle },
    ) {
        SheetTop(
            model = model,
            chrome = chrome,
            onEvent = onEvent,
        )

        if (spec.bodyWrapped) {
            Column(
                modifier = Modifier
                    .weight(
                        weight = 1f,
                        fill = false,
                    )
                    .then(if (spec.bodyScrolls) Modifier.verticalScroll(state = scrollState) else Modifier),
                content = content,
            )
        } else {
            content()
        }

        if (model.footer != null) {
            if (spec.footerSeparated) {
                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            SheetFooter(
                model = model.footer,
                onEvent = onEvent,
                chrome = chrome,
            )
        }
    }
}

/** Where a [SheetLayout] places the scroll and the footer divider. */
private data class SheetLayoutSpec(
    val wholeSheetScrolls: Boolean,
    val bodyWrapped: Boolean,
    val bodyScrolls: Boolean,
    val footerSeparated: Boolean,
)

private fun SheetLayout.spec(): SheetLayoutSpec = when (this) {
    SheetLayout.Scrolling -> SheetLayoutSpec(
        wholeSheetScrolls = true,
        bodyWrapped = false,
        bodyScrolls = false,
        footerSeparated = false,
    )

    SheetLayout.Pinned -> SheetLayoutSpec(
        wholeSheetScrolls = false,
        bodyWrapped = true,
        bodyScrolls = true,
        footerSeparated = true,
    )

    SheetLayout.PinnedLazy -> SheetLayoutSpec(
        wholeSheetScrolls = false,
        bodyWrapped = true,
        bodyScrolls = false,
        footerSeparated = true,
    )
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

    SheetHeader(
        model = model.header,
        chrome = chrome,
        onEvent = onEvent,
    )

    Spacer(modifier = Modifier.height(24.dp))
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
                Spacer(modifier = Modifier.width(8.dp))

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
            onClick = { onEvent(SheetScaffoldEvent.HeaderActionClicked) },
            enabled = chrome.unlocked,
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
                    .offset(x = 26.dp * side)
                    .width(52.dp)
                    .rotate(10f * side),
            )
        }
    }
}

/** Fan side per jacket index: the first upright in front, then one behind on each side. */
private val JACKET_FAN = listOf(0, -1, 1)

/** Internal so the gallery can show the footer on its own. */
@Composable
internal fun SheetFooter(
    model: SheetFooterUiModel,
    onEvent: (SheetScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    chrome: SheetChrome = rememberSheetChrome(),
) {
    when (model) {
        is SheetFooterUiModel.Stacked -> Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (model.primary != null) {
                PrimaryFooterButton(
                    button = model.primary,
                    chrome = chrome,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (model.secondary != null) {
                SecondaryFooterButton(
                    button = model.secondary,
                    chrome = chrome,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        is SheetFooterUiModel.Inline -> Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SecondaryFooterButton(
                button = model.secondary,
                chrome = chrome,
                onEvent = onEvent,
            )

            PrimaryFooterButton(
                button = model.primary,
                chrome = chrome,
                onEvent = onEvent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PrimaryFooterButton(
    button: SheetFooterButton,
    chrome: SheetChrome,
    onEvent: (SheetScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetFooterButtonView(
        button = button,
        style = ButtonStyle.FILLED,
        chrome = chrome,
        loading = chrome.busy,
        onClick = { onEvent(SheetScaffoldEvent.PrimaryClicked) },
        modifier = modifier,
    )
}

@Composable
private fun SecondaryFooterButton(
    button: SheetFooterButton,
    chrome: SheetChrome,
    onEvent: (SheetScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetFooterButtonView(
        button = button,
        style = ButtonStyle.TEXT,
        chrome = chrome,
        loading = false,
        onClick = { onEvent(SheetScaffoldEvent.SecondaryClicked) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SheetFooterButtonView(
    button: SheetFooterButton,
    style: ButtonStyle,
    chrome: SheetChrome,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.then(
            if (loading) {
                Modifier.semantics { stateDescription = chrome.busyDescription }
            } else {
                Modifier
            },
        ),
        contentAlignment = Alignment.CenterEnd,
    ) {
        RhaydusButton(
            label = button.label,
            style = style,
            onClick = {
                onClick()

                if (button.dismissesSheet) {
                    chrome.dismiss()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = button.enabled && chrome.busy.not() && chrome.unlocked,
            size = ButtonSize.M,
        )

        if (loading) {
            CircularWavyProgressIndicator(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(20.dp),
            )
        }
    }
}
