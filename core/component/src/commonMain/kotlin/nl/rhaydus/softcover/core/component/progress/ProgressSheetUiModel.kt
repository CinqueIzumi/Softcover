package nl.rhaydus.softcover.core.component.progress

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.control.SegmentUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything `UpdateProgressBottomSheet` renders: the book it is logging against, what that book's
 * progress is measured in, and which entry tab is showing.
 *
 * [tabs] carries the entry tabs the medium offers and the one showing; build it with [tabsFor]. The
 * showing tab lives here rather than beside the model as a second parameter because it is part of
 * what the sheet draws, and the contract's § 7.1 signature admits only `model`, `onEvent` and
 * `modifier`. Its single *stored* home is the hosting feature's `UiState`; this field is derived from
 * it, so there is one writer and the two cannot drift.
 *
 * [progressPercent] is whole percent (0-100), already rounded — the sheet never sees the underlying
 * float.
 *
 * [whenRead] is the backdating row shared by every tab.
 */
@Immutable
data class ProgressSheetUiModel(
    val medium: ProgressSheetMedium,
    val progressPercent: Int,
    val tabs: SegmentedControlUiModel<ProgressSheetTab>,
    val whenRead: WhenReadUiModel,
    val scaffold: SheetScaffoldUiModel,
) {
    companion object : UiModelPreviews<ProgressSheetUiModel> {
        /**
         * Percentage entry needs a known total to convert a fraction into pages (or seconds); without one
         * it can only ever record 0, so it is offered only when that total is known. The primary unit —
         * time for an audiobook, pages otherwise — always stays, allowing free entry even with no total.
         * A [selectedTab] that is not offered (a stored PAGE on an audiobook) falls back to the primary one.
         */
        fun tabsFor(
            medium: ProgressSheetMedium,
            selectedTab: ProgressSheetTab,
        ): SegmentedControlUiModel<ProgressSheetTab> {
            val primaryTab = when (medium) {
                is ProgressSheetMedium.Timed -> ProgressSheetTab.TIME
                is ProgressSheetMedium.Paged -> ProgressSheetTab.PAGE
            }

            val offered = if (medium.hasKnownTotal) {
                listOf(primaryTab, ProgressSheetTab.PERCENTAGE)
            } else {
                listOf(primaryTab)
            }

            return SegmentedControlUiModel(
                options = offered
                    .map { tab ->
                        SegmentUiModel(
                            key = tab,
                            label = tab.tabName,
                        )
                    }
                    .toImmutableList(),
                selected = if (selectedTab in offered) selectedTab else primaryTab,
            )
        }

        private fun fixture(
            bookTitle: String,
            medium: ProgressSheetMedium,
            progressPercent: Int,
            selectedTab: ProgressSheetTab,
        ): ProgressSheetUiModel = ProgressSheetUiModel(
            medium = medium,
            progressPercent = progressPercent,
            tabs = tabsFor(
                medium = medium,
                selectedTab = selectedTab,
            ),
            whenRead = WhenReadUiModel(
                chip = ChipUiModel(
                    key = "when-read",
                    label = "Just now",
                    leading = ChipLeading.Icon(icon = SoftcoverIcon.DateRange),
                ),
                dayLabel = "Today",
                timeLabel = "09:41",
                canShiftLater = false,
            ),
            scaffold = SheetScaffoldUiModel(
                header = SheetHeaderUiModel(
                    section = SectionHeaderUiModel.Section(
                        eyebrow = "Reading progress",
                        headline = bookTitle,
                    ),
                ),
            ),
        )

        /**
         * One fixture per branch that changes the sheet's anatomy — not per string. The two
         * "no total" fixtures are the interesting ones: with no page count or runtime to divide by,
         * the sheet drops the Percentage tab entirely and hides the suffix line and the progress
         * indicator, so they exercise a different layout rather than different copy.
         */
        override val previews: ImmutableMap<String, ProgressSheetUiModel> = persistentMapOf(
            "Pages" to fixture(
                bookTitle = "The Dungeon Anarchist's Cookbook",
                medium = ProgressSheetMedium.Paged(
                    totalPages = 534,
                    currentPage = 80,
                    suffix = "of 534 pages",
                ),
                progressPercent = 15,
                selectedTab = ProgressSheetTab.PAGE,
            ),
            "Percentage" to fixture(
                bookTitle = "The Dungeon Anarchist's Cookbook",
                medium = ProgressSheetMedium.Paged(
                    totalPages = 534,
                    currentPage = 187,
                    suffix = "of 534 pages",
                ),
                progressPercent = 35,
                selectedTab = ProgressSheetTab.PERCENTAGE,
            ),
            "Paged, no total" to fixture(
                bookTitle = "Piranesi",
                medium = ProgressSheetMedium.Paged(
                    totalPages = 0,
                    currentPage = 42,
                    suffix = null,
                ),
                progressPercent = 0,
                selectedTab = ProgressSheetTab.PAGE,
            ),
            "Audiobook" to fixture(
                bookTitle = "The Name of the Wind",
                medium = ProgressSheetMedium.Timed(
                    totalSeconds = 100_320,
                    hours = progressTimeField(
                        unit = ProgressTimeUnit.HOURS,
                        text = "3",
                    ),
                    minutes = progressTimeField(
                        unit = ProgressTimeUnit.MINUTES,
                        text = "20",
                    ),
                    seconds = progressTimeField(
                        unit = ProgressTimeUnit.SECONDS,
                        text = "45",
                    ),
                    suffix = "of 27:52:00",
                    fraction = 0.12f,
                    canDecreaseMinutes = true,
                    canIncreaseMinutes = true,
                ),
                progressPercent = 12,
                selectedTab = ProgressSheetTab.TIME,
            ),
            "Audiobook, no total" to fixture(
                bookTitle = "The Name of the Wind",
                medium = ProgressSheetMedium.Timed(
                    totalSeconds = 0,
                    hours = progressTimeField(
                        unit = ProgressTimeUnit.HOURS,
                        text = "1",
                    ),
                    minutes = progressTimeField(
                        unit = ProgressTimeUnit.MINUTES,
                        text = "0",
                    ),
                    seconds = progressTimeField(
                        unit = ProgressTimeUnit.SECONDS,
                        text = "0",
                    ),
                    suffix = null,
                    fraction = 0f,
                    canDecreaseMinutes = false,
                    canIncreaseMinutes = true,
                ),
                progressPercent = 0,
                selectedTab = ProgressSheetTab.TIME,
            ),
        )
    }
}
