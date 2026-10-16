package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import nl.rhaydus.softcover.core.personal.domain.repository.QuoteRepository
import kotlin.time.Instant

class AddQuoteUseCase(
    private val repository: QuoteRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        editionId: Int?,
        text: String,
        privacy: PrivacySetting,
        actionAt: Instant?,
        position: QuotePosition?,
    ): Result<Long> = runCatchingLogged {
        repository.add(
            bookId = bookId,
            editionId = editionId,
            text = text,
            privacy = privacy,
            actionAt = actionAt,
            position = position,
        )
    }
}
