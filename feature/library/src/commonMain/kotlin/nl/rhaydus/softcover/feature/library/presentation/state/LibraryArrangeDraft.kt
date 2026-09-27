package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection

/**
 * The Arrange sheet's uncommitted draft, seeded from committed state when the sheet opens
 * ([nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeSheetExpandedChangeAction]),
 * mutated by chip taps and the titles toggle, and committed by
 * [nl.rhaydus.softcover.feature.library.presentation.action.OnApplyArrangeAction]. Cleared on close
 * without applying.
 */
internal data class LibraryArrangeDraft(
    val tabId: String,
    val sortMode: LibrarySortMode,
    val sortDirection: SortDirection,
    val gridLayout: LibraryGridLayout,
)
