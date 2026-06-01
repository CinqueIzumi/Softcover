package nl.rhaydus.softcover.core.preferences.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection

@Serializable
data class AppSettingsEntity(
    // Kept only so ApiKeyLocalDataSource can migrate pre-1.2.3 plaintext keys into the Keystore-backed store. Cleared after migration.
    val apiKey: String = "",
    val userId: Int = -1,
    val themeConfig: ThemeConfigurationEntity = ThemeConfigurationEntity(),
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    val libraryGridLayout: LibraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
    val librarySortModeByTab: Map<String, LibrarySortMode> = emptyMap(),
    val librarySortDirectionByTab: Map<String, SortDirection> = emptyMap(),
    val dismissedPlanTodayByBook: Map<Int, String> = emptyMap(),
    val enabledStatusCodes: Set<Int> = setOf(1, 3, 5),
    val enabledListIds: Set<Int> = emptySet(),
    val listDefaultsSeeded: Boolean = false,
    val libraryTabOrder: List<String> = emptyList(),
)