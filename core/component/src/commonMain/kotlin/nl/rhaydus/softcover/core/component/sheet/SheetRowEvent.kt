package nl.rhaydus.softcover.core.component.sheet

import nl.rhaydus.softcover.core.component.field.TextFieldEvent

sealed interface SheetRowEvent<out K> {
    data class Selected<K>(val key: K) : SheetRowEvent<K>

    data class InputChanged(val event: TextFieldEvent) : SheetRowEvent<Nothing>
}
