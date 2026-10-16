package nl.rhaydus.softcover.feature.widget.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

internal class RandomPickResolution(
    val book: Book?,
    val source: RandomPickSource,
)
