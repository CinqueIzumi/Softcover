package nl.rhaydus.softcover.feature.book_detail.presentation.state

internal sealed interface WhereYouGotItBody {
    data object Choices : WhereYouGotItBody

    data class Currency(val query: String) : WhereYouGotItBody
}
