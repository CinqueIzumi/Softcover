package nl.rhaydus.softcover.feature.personal.domain.usecase

import nl.rhaydus.softcover.feature.personal.domain.repository.HighlightRepository

class AddHighlightUseCase(
    private val repository: HighlightRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        quote: String,
        page: Int? = null,
        note: String? = null,
    ): Long = repository.add(
        bookId = bookId,
        quote = quote,
        page = page,
        note = note,
    )
}
