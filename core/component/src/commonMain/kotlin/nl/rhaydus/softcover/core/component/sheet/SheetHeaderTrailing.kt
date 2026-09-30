package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

@Immutable
sealed interface SheetHeaderTrailing {
    data class Jacket(val cover: CoverUiModel) : SheetHeaderTrailing

    /** Up to three jackets, fanned; further covers are not drawn. */
    data class JacketStack(val covers: ImmutableList<CoverUiModel>) : SheetHeaderTrailing

    data class Action(val label: String) : SheetHeaderTrailing
}
