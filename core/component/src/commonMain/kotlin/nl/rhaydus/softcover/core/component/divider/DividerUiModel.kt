package nl.rhaydus.softcover.core.component.divider

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** Which hairline rule a [Divider] renders. */
@Immutable
sealed interface DividerUiModel {
    data class Horizontal(
        val ink: DividerInk = DividerInk.Outline,
        val inset: Dp = 0.dp,
        val center: DividerCenter? = null,
    ) : DividerUiModel

    data class Vertical(
        val ink: DividerInk = DividerInk.Outline,
        val length: Dp? = null,
    ) : DividerUiModel

    companion object : UiModelPreviews<DividerUiModel> {
        override val previews: ImmutableList<DividerUiModel> = persistentListOf(
            Horizontal(),
            Horizontal(inset = 40.dp),
            Horizontal(ink = DividerInk.ContentTint(alpha = 0.25f)),
            Horizontal(ink = DividerInk.OnImage(alpha = 0.3f)),
            Horizontal(center = DividerCenter.QuoteMark),
            Horizontal(center = DividerCenter.Label(text = "Or type it")),
            Vertical(),
            Vertical(length = 32.dp),
        )
    }
}
