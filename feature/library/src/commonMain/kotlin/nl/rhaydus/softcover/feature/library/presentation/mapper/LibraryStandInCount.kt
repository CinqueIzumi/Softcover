package nl.rhaydus.softcover.feature.library.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout

/** How many loading cards the grid shows for this layout: the number its platform grid fills before data lands. */
internal expect fun LibraryGridLayout.standInCount(): Int
