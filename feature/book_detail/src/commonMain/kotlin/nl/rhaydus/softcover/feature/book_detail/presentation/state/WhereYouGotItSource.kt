package nl.rhaydus.softcover.feature.book_detail.presentation.state

internal sealed interface WhereYouGotItSource {
    data object None : WhereYouGotItSource

    data class Preset(val key: String) : WhereYouGotItSource

    data class Custom(val text: String) : WhereYouGotItSource
}
