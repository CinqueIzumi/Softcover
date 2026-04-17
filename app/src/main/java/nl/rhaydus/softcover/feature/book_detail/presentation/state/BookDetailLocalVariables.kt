package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.presentation.toad.LocalVariables

data class BookDetailLocalVariables(
    val editionsLoadedForBookId: Int? = null,
) : LocalVariables
