package nl.rhaydus.softcover.core.component.divider

sealed interface DividerCenter {
    data object QuoteMark : DividerCenter

    data class Label(val text: String) : DividerCenter
}
