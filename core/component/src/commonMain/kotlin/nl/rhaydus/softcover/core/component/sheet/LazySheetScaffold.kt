package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * A [SheetScaffold] whose body is one lazy list, for a [SheetLayout.Pinned] [model].
 *
 * The scaffold owns the `LazyColumn`: [body] adds items to it, and [state] is hoisted so a caller can
 * observe or drive the scroll. [pinned] sits fixed above the list, under the header. With a footer the list
 * keeps a [Spacing.md] bottom content padding, so rows scroll right up to the footer divider and still
 * rest that far clear of it at the end. The list is as tall as its items unless [fillHeight] is set, which
 * gives it all the height the sheet leaves so a changing item count does not resize the sheet.
 */
@Composable
fun LazySheetScaffold(
    model: SheetScaffoldUiModel,
    onEvent: (SheetScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    fillHeight: Boolean = false,
    pinned: @Composable ColumnScope.() -> Unit = {},
    body: LazyListScope.() -> Unit,
) {
    require(model.layout == SheetLayout.Pinned) { "LazySheetScaffold needs SheetLayout.Pinned" }

    SheetScaffoldModal(
        model = model,
        onEvent = onEvent,
        modifier = modifier,
        lazyBody = true,
    ) {
        LazySheetBody(
            model = model,
            state = state,
            fillHeight = fillHeight,
            pinned = pinned,
            body = body,
        )
    }
}

/** The pinned slot and the lazy list of a [LazySheetScaffold], so the gallery can host them inline. */
@Composable
internal fun ColumnScope.LazySheetBody(
    model: SheetScaffoldUiModel,
    state: LazyListState,
    fillHeight: Boolean,
    pinned: @Composable ColumnScope.() -> Unit,
    body: LazyListScope.() -> Unit,
) {
    val hasFooter = model.footer != null
    val contentPadding = remember(hasFooter) { PaddingValues(bottom = if (hasFooter) Spacing.md else 0.dp) }

    pinned()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(
                weight = 1f,
                fill = fillHeight,
            ),
        state = state,
        contentPadding = contentPadding,
        content = body,
    )
}
