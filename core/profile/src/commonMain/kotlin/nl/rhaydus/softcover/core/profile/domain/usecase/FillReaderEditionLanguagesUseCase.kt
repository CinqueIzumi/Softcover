package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.usecase.FillEditionLanguagesUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetPausedUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase

class FillReaderEditionLanguagesUseCase internal constructor(
    private val getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    private val getPausedUserBooksUseCase: GetPausedUserBooksUseCase,
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
    private val fillEditionLanguagesUseCase: FillEditionLanguagesUseCase,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        val books = combine(
            getCurrentlyReadingUserBooksUseCase(),
            getPausedUserBooksUseCase(),
            getReadUserBooksUseCase(),
            getDidNotFinishUserBooksUseCase(),
        ) { reading, paused, read, didNotFinish -> reading + paused + read + didNotFinish }
            .firstOrNull()
            .orEmpty()

        val editionIds = books
            .mapNotNull { it.readerEdition }
            .filterNot { it.isLanguageFetched }
            .map { it.id }
            .toSet()

        if (editionIds.isNotEmpty()) {
            fillEditionLanguagesUseCase(editionIds = editionIds).getOrThrow()
        }
    }
}
