package nl.rhaydus.softcover.core.component.badge

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [DeadlineSummaryLine] renders: the deadline date and the pace needed to still make it,
 * already formatted by the mapper — "28 Aug" and "18 pages/day" become one line, "28 Aug • 18
 * pages/day" — or the status label alone once the deadline has passed.
 *
 * @property dateText The deadline date, already formatted with the reader's own date style — the
 * mapper's job (§ 7.2 R4), not this component's.
 * @property paceText The pace needed to make the deadline ("18 pages/day", "12m/day"), or the status
 * label ("Expired") once the deadline has passed — the mapper already decided which.
 * @property tone Which ink the line paints in — see [DeadlineSummaryTone].
 */
@Immutable
data class DeadlineSummaryUiModel(
    val dateText: String,
    val paceText: String,
    val tone: DeadlineSummaryTone,
) {
    companion object : UiModelPreviews<DeadlineSummaryUiModel> {
        /**
         * Per R5: an on-track pages pace, an on-track audio pace (the `m`/`h` unit), an expired
         * deadline (the status label instead of a pace), the hero-backdrop tone, and a pace extreme
         * enough ("1149 pages/day") to exercise the line's single-line ellipsis.
         */
        override val previews: ImmutableList<DeadlineSummaryUiModel> = persistentListOf(
            DeadlineSummaryUiModel(
                dateText = "28 Aug",
                paceText = "18 pages/day",
                tone = DeadlineSummaryTone.OnSurface,
            ),
            DeadlineSummaryUiModel(
                dateText = "28 Aug",
                paceText = "12m/day",
                tone = DeadlineSummaryTone.OnSurface,
            ),
            DeadlineSummaryUiModel(
                dateText = "12 Jul",
                paceText = "Expired",
                tone = DeadlineSummaryTone.OnSurface,
            ),
            DeadlineSummaryUiModel(
                dateText = "28 Aug",
                paceText = "18 pages/day",
                tone = DeadlineSummaryTone.OnHeroBackdrop,
            ),
            DeadlineSummaryUiModel(
                dateText = "1 Sep",
                paceText = "1149 pages/day",
                tone = DeadlineSummaryTone.OnSurface,
            ),
        )
    }
}
