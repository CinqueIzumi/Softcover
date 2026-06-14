package nl.rhaydus.softcover.orchestration.presentation

// Desktop Library is a bespoke full-width shelf-sidebar + adaptive cover grid; a side detail pane
// would only starve the grid (and sit empty until a book is picked). So desktop opts the Library tab
// out of the two-pane and opens a tapped book as a full-screen pushed detail screen instead.
internal actual val libraryUsesDetailPane: Boolean = false
