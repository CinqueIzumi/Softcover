package nl.rhaydus.softcover.feature.explore.presentation.model

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.feature.explore.presentation.state.becauseYouReadGenreSheetScaffold

/**
 * @property visible False when no genre has resolved, which hides the whole "because you read" section.
 * @property isLoading True while the section's books are still loading, which shows the control as a skeleton.
 * @property genreGroup A `null` key is the "Auto (most-read)" row.
 */
internal data class BecauseYouReadGenreControlUiModel(
    val label: String = becauseYouReadGenreLabelFor(genre = null),
    val visible: Boolean = false,
    val isLoading: Boolean = false,
    val hasOptions: Boolean = false,
    val sheetVisible: Boolean = false,
    val sheet: SheetScaffoldUiModel = becauseYouReadGenreSheetScaffold(),
    val genreGroup: SheetRowGroupUiModel<String?> = SheetRowGroupUiModel(rows = persistentListOf()),
)

internal fun becauseYouReadGenreLabelFor(genre: String?): String =
    "Genre · ${genre ?: BECAUSE_YOU_READ_FALLBACK_GENRE}".uppercase()

private const val BECAUSE_YOU_READ_FALLBACK_GENRE = "Fantasy"
