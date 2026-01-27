package nl.rhaydus.softcover.feature.reading.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCaseAsFlow
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetCurrentlyReadingBooksUseCase @Inject constructor(
    private val booksRepository: BooksRepository,
    private val getUserIdUseCaseAsFlow: GetUserIdUseCaseAsFlow,
) {
    operator fun invoke(): Flow<List<Book>> {
        return getUserIdUseCaseAsFlow().flatMapLatest { userId ->
            booksRepository.getCurrentlyReadingBooks(userId = userId)
        }
    }
}