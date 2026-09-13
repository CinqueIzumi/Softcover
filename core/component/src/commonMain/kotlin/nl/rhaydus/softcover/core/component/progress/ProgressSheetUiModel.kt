package nl.rhaydus.softcover.core.component.progress

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything `UpdateProgressBottomSheet` renders: the book it is logging against, what that book's
 * progress is measured in, and which entry tab is showing.
 *
 * [selectedTab] lives here rather than beside the model as a second parameter because it is part of
 * what the sheet draws, and the contract's § 7.1 signature admits only `model`, `onEvent` and
 * `modifier`. Its single *stored* home is the hosting feature's `UiState`; this field is derived from
 * it, so there is one writer and the two cannot drift.
 *
 * [progressPercent] is whole percent (0-100), already rounded — the sheet never sees the underlying
 * float.
 */
@Immutable
data class ProgressSheetUiModel(
    val bookTitle: String,
    val medium: ProgressSheetMedium,
    val progressPercent: Int,
    val selectedTab: ProgressSheetTab,
) {
    companion object : UiModelPreviews<ProgressSheetUiModel> {
        /**
         * Per R5, one fixture per branch that changes the sheet's anatomy — not per string. The two
         * "no total" fixtures are the interesting ones: with no page count or runtime to divide by,
         * the sheet drops the Percentage tab entirely and hides the suffix line and the progress
         * indicator, so they exercise a different layout rather than different copy.
         */
        override val previews: ImmutableList<ProgressSheetUiModel> = persistentListOf(
            ProgressSheetUiModel(
                bookTitle = "The Dungeon Anarchist's Cookbook",
                medium = ProgressSheetMedium.Paged(
                    totalPages = 534,
                    currentPage = 80,
                ),
                progressPercent = 15,
                selectedTab = ProgressSheetTab.PAGE,
            ),
            ProgressSheetUiModel(
                bookTitle = "The Dungeon Anarchist's Cookbook",
                medium = ProgressSheetMedium.Paged(
                    totalPages = 534,
                    currentPage = 187,
                ),
                progressPercent = 35,
                selectedTab = ProgressSheetTab.PERCENTAGE,
            ),
            ProgressSheetUiModel(
                bookTitle = "Piranesi",
                medium = ProgressSheetMedium.Paged(
                    totalPages = 0,
                    currentPage = 42,
                ),
                progressPercent = 0,
                selectedTab = ProgressSheetTab.PAGE,
            ),
            ProgressSheetUiModel(
                bookTitle = "The Name of the Wind",
                medium = ProgressSheetMedium.Timed(
                    totalSeconds = 100_320,
                    currentSeconds = 12_045,
                ),
                progressPercent = 12,
                selectedTab = ProgressSheetTab.TIME,
            ),
            ProgressSheetUiModel(
                bookTitle = "The Name of the Wind",
                medium = ProgressSheetMedium.Timed(
                    totalSeconds = 0,
                    currentSeconds = 3_600,
                ),
                progressPercent = 0,
                selectedTab = ProgressSheetTab.TIME,
            ),
        )
    }
}
