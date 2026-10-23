package nl.rhaydus.softcover.showcase.live.topbar.model



internal sealed interface TopbarRow {
    data class Bar(val mode: TopbarMode) : TopbarRow

    data object Appearance : TopbarRow
}
