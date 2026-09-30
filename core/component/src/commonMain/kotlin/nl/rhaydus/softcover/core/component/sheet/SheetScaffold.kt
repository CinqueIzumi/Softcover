package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader

@Composable
fun SheetScaffold(
    model: SheetScaffoldUiModel,
    onEvent: (SheetScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AdaptiveModalSheet(
        onDismissRequest = { onEvent(SheetScaffoldEvent.Dismissed) },
        modifier = modifier,
        dismissOnTapOutside = model.dismissible,
        dismissOnBackPress = model.dismissible,
    ) {
        SheetScaffoldContent(
            model = model,
            onEvent = onEvent,
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
    content: @Composable ColumnScope.() -> Unit,
) {
    val padding = Modifier
        .padding(horizontal = 24.dp)
        .padding(top = 8.dp, bottom = 16.dp)

    when (model.layout) {
        SheetLayout.Scrolling -> Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .imePadding()
                .then(padding),
        ) {
            SheetTop(
                model = model,
                onEvent = onEvent,
            )

            content()

            if (model.footer != null) {
                Spacer(modifier = Modifier.height(24.dp))

                SheetFooter(
                    model = model.footer,
                    onEvent = onEvent,
                )
            }
        }

        SheetLayout.Pinned -> Column(
            modifier = modifier
                .fillMaxWidth()
                .imePadding()
                .then(padding),
        ) {
            SheetTop(
                model = model,
                onEvent = onEvent,
            )

            Column(
                modifier = Modifier.weight(
                    weight = 1f,
                    fill = false,
                ),
                content = content,
            )

            if (model.footer != null) {
                Divider(model = DividerUiModel.Horizontal())

                Spacer(modifier = Modifier.height(12.dp))

                SheetFooter(
                    model = model.footer,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun SheetTop(
    model: SheetScaffoldUiModel,
    onEvent: (SheetScaffoldEvent) -> Unit,
) {
    if (model.topAction != null) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                text = model.topAction,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .pointerHandCursor()
                    .clickable(
                        role = Role.Button,
                        onClick = { onEvent(SheetScaffoldEvent.TopActionClicked) },
                    )
                    .minimumInteractiveComponentSize(),
            )
        }
    }

    SheetHeader(
        model = model.header,
        onEvent = onEvent,
    )

    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun SheetHeader(
    model: SheetHeaderUiModel,
    onEvent: (SheetScaffoldEvent) -> Unit,
) {
    val trailing = model.trailing

    SectionHeader(
        model = model.section,
        trailing = if (trailing == null) {
            null
        } else {
            {
                Spacer(modifier = Modifier.width(12.dp))

                SheetHeaderTrailingContent(
                    trailing = trailing,
                    onEvent = onEvent,
                )
            }
        },
    )
}

@Composable
private fun RowScope.SheetHeaderTrailingContent(
    trailing: SheetHeaderTrailing,
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
        )
    }
}

@Composable
private fun JacketStack(trailing: SheetHeaderTrailing.JacketStack) {
    val rotations = listOf(-8f, 4f, 0f)

    Box(
        modifier = Modifier.size(
            width = 84.dp,
            height = 84.dp,
        ),
        contentAlignment = Alignment.Center,
    ) {
        trailing.covers.take(rotations.size).forEachIndexed { index, cover ->
            Cover(
                model = cover,
                modifier = Modifier
                    .width(56.dp)
                    .rotate(rotations[index]),
            )
        }
    }
}

/** Internal so the gallery can show the footer on its own. */
@Composable
internal fun SheetFooter(
    model: SheetFooterUiModel,
    onEvent: (SheetScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (model.layout) {
        SheetFooterLayout.Stacked -> Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (model.primary != null) {
                SheetFooterButtonView(
                    button = model.primary,
                    style = ButtonStyle.FILLED,
                    onClick = { onEvent(SheetScaffoldEvent.PrimaryClicked) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (model.secondary != null) {
                SheetFooterButtonView(
                    button = model.secondary,
                    style = ButtonStyle.TEXT,
                    onClick = { onEvent(SheetScaffoldEvent.SecondaryClicked) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        SheetFooterLayout.Inline -> Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (model.secondary != null) {
                SheetFooterButtonView(
                    button = model.secondary,
                    style = ButtonStyle.TEXT,
                    onClick = { onEvent(SheetScaffoldEvent.SecondaryClicked) },
                )
            }

            if (model.primary != null) {
                SheetFooterButtonView(
                    button = model.primary,
                    style = ButtonStyle.FILLED,
                    onClick = { onEvent(SheetScaffoldEvent.PrimaryClicked) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SheetFooterButtonView(
    button: SheetFooterButton,
    style: ButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd,
    ) {
        RhaydusButton(
            label = button.label,
            style = style,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = button.enabled && button.loading.not(),
            size = ButtonSize.M,
        )

        if (button.loading) {
            CircularWavyProgressIndicator(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(20.dp),
            )
        }
    }
}
