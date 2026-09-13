package nl.rhaydus.softcover.core.component.lists

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * One jacket the choose-lists header wants drawn, handed to the caller's `jacket` slot.
 *
 * The sheet needs a book cover, and resolving one takes the reader's chosen edition, the book's
 * default, a fallback URL and a locally persisted file — none of which a component may know (R4). So
 * the caller supplies the image and the sheet supplies the treatment: this carrier plus the
 * `Modifier` alongside it, which already holds the jacket's width and its rotation in the stack.
 *
 * The treatment travels *into* the slot rather than being published as defaults the caller reads,
 * because two callers left to apply the same treatment themselves drift apart — the lesson from
 * `VerdictSheetCoverDefaults`. Neither caller can get this wrong: it has nothing to decide.
 *
 * [index] runs from `0`, and the caller maps it onto whichever cover it holds for that position. The
 * header asks for [ChooseListsVariant.ManyBooks.coverCount] jackets — or a single one when that is
 * zero, so a selection whose books resolve no cover at all still shows a coverless placeholder.
 */
@Immutable
data class ChooseListsJacket(
    val index: Int,
    val cornerRadius: Dp,
    val elevation: Dp,
    val shadowColor: Color,
)
