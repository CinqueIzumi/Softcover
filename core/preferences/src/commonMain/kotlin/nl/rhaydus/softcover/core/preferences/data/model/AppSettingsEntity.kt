package nl.rhaydus.softcover.core.preferences.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.core.preferences.data.datastore.serializer.LibrarySortModeMapSerializer
import nl.rhaydus.softcover.core.preferences.data.datastore.serializer.SortDirectionMapSerializer

@Serializable
internal data class AppSettingsEntity(
    // Kept only so ApiKeyLocalDataSource can migrate pre-1.2.3 plaintext keys into the Keystore-backed store. Cleared after migration.
    val apiKey: String = "",
    val userId: Int = -1,
    val themeConfig: ThemeConfigurationEntity = ThemeConfigurationEntity(),
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    val libraryGridLayout: LibraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
    @Serializable(with = LibrarySortModeMapSerializer::class)
    val librarySortModeByTab: Map<String, LibrarySortMode> = emptyMap(),
    @Serializable(with = SortDirectionMapSerializer::class)
    val librarySortDirectionByTab: Map<String, SortDirection> = emptyMap(),
    val dismissedPlanTodayByBook: Map<Int, String> = emptyMap(),
    val enabledStatusCodes: Set<Int> = setOf(1, 3, 4, 5),
    val pausedShelfSeeded: Boolean = false,
    val enabledListIds: Set<Int> = emptySet(),
    val listDefaultsSeeded: Boolean = false,
    // False until one user-book refresh across every status has succeeded on this install.
    val readThroughsBackfilled: Boolean = false,
    val libraryTabOrder: List<String> = emptyList(),
    val desktopWindowState: DesktopWindowStateEntity = DesktopWindowStateEntity(),
    val readingStreakEnabled: Boolean = true,
    val shelfSwipeEnabled: Boolean = false,
    // Off by default: the author breakdown counts every author it knows about, untagged ones included,
    // so the untagged share stays visible until the reader asks for the tagged-only reading.
    val hideUntaggedAuthors: Boolean = false,
    val lastUsedProgressUnit: ProgressUnit = ProgressUnit.PAGE,
    val uiScale: UiScale = UiScale.DEFAULT,
    // Null means "use the auto-derived most-read genre" - see GetBecauseYouReadBooksUseCase.
    val becauseYouReadGenre: String? = null,
)
