package nl.rhaydus.softcover.orchestration.presentation

// Mobile/tablet keeps the Library two-pane on expanded windows (large tablets), where a list + detail
// spread reads well within touch reach. Desktop overrides this to push full-screen — see the jvm actual.
internal actual val libraryUsesDetailPane: Boolean = true
