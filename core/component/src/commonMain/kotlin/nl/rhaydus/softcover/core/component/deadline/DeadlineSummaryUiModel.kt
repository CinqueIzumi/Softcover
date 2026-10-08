package nl.rhaydus.softcover.core.component.deadline

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [DeadlineSummaryLine] renders.
 *
 * @property text The line, already formatted by the mapper: the deadline date and the pace needed to
 * make it ("28 Aug • 18 pages/day"), or the status label in place of the pace once it has passed
 * ("12 Jul • Expired").
 */
@Immutable
data class DeadlineSummaryUiModel(
    val text: String,
) {
    companion object : UiModelPreviews<DeadlineSummaryUiModel> {
        override val previews: ImmutableMap<String, DeadlineSummaryUiModel> = persistentMapOf(
            "On track" to DeadlineSummaryUiModel(
                text = "28 Aug • 18 pages/day",
            ),
        )
    }
}
