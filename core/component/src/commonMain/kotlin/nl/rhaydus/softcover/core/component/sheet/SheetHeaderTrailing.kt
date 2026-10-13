package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

@Immutable
sealed interface SheetHeaderTrailing {
    data class Jacket(val cover: CoverUiModel) : SheetHeaderTrailing

    /** Up to [MAX_COVERS] jackets, fanned; the builder trims the selection to that cap. */
    data class JacketStack(val covers: ImmutableList<CoverUiModel>) : SheetHeaderTrailing {
        init {
            require(covers.size <= MAX_COVERS) { "A jacket stack fans at most $MAX_COVERS covers" }
        }

        companion object {
            const val MAX_COVERS: Int = 3
        }
    }

    /** [dismissesSheet] works as [SheetFooterButton.dismissesSheet] does. */
    data class Action(
        val label: String,
        val dismissesSheet: Boolean = false,
    ) : SheetHeaderTrailing
}
