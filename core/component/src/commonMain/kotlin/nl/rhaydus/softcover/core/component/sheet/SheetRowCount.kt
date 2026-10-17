package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable

/** A [SheetRowUiModel]'s trailing count: the [text] shown and the [description] a screen reader announces in its place. */
@Immutable
data class SheetRowCount(
    val text: String,
    val description: String,
)
