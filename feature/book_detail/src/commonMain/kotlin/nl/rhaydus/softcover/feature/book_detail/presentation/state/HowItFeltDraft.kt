package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.domain.model.BookMood

internal data class HowItFeltDraft(
    val moods: PersistentList<BookMood> = persistentListOf(),
    val ownWordOpen: Boolean = false,
    val ownWordText: String = "",
)
