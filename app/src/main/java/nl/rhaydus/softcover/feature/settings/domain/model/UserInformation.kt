package nl.rhaydus.softcover.feature.settings.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

data class UserInformation(
    val id: Int,
    val books: List<Book>,
)