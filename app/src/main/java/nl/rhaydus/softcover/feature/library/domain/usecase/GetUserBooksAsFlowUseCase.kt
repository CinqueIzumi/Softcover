package nl.rhaydus.softcover.feature.library.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.library.domain.repository.LibraryRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCaseAsFlow

class GetUserBooksAsFlowUseCase(
    private val libraryRepository: LibraryRepository,
    private val getUserIdUseCaseAsFlow: GetUserIdUseCaseAsFlow,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(): Flow<List<Book>> {
        return getUserIdUseCaseAsFlow().flatMapLatest {
            libraryRepository.getUserBooks(userId = it)
        }
    }
}