package nl.rhaydus.softcover.feature.book_detail.presentation.state

internal sealed interface WhereYouGotItDraft {
    data object None : WhereYouGotItDraft

    data class Preset(val key: String) : WhereYouGotItDraft

    data class Custom(val text: String) : WhereYouGotItDraft
}
