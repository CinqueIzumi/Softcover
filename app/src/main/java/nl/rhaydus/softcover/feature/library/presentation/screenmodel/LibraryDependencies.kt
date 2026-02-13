package nl.rhaydus.softcover.feature.updated_library.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RefreshUserBooksUseCase

class LibraryDependencies(
    val getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase,
    val getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    val getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
    val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    val refreshUserBooksUseCase: RefreshUserBooksUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()