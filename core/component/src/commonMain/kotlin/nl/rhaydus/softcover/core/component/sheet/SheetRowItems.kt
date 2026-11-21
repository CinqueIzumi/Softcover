package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import kotlinx.collections.immutable.ImmutableList

fun <K : Any> LazyListScope.sheetRowItems(
    rows: ImmutableList<SheetRowUiModel<K>>,
    onEvent: (SheetRowEvent<K>) -> Unit,
) {
    items(items = rows, key = { it.key }) { row ->
        SheetRow(
            model = row,
            onEvent = onEvent,
        )
    }
}
