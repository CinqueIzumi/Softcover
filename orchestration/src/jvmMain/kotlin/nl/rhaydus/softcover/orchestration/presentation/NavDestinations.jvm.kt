package nl.rhaydus.softcover.orchestration.presentation

// Desktop Library and Explore are bespoke full-width layouts (shelf-sidebar + adaptive cover grid,
// and the discovery grids respectively); a side detail pane would only starve the grid (and sit empty
// until a book is picked). So desktop opts both wide-grid tabs out of the two-pane and opens a tapped
// book as a full-screen pushed detail screen instead.
internal actual val wideGridTabsUseDetailPane: Boolean = false
