package nl.rhaydus.softcover.feature.library.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.InitializeUserBooksUseCase

class LibraryDependencies(
    val getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase,
    val getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    val getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
    val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    val initializeUserBooksUseCase: InitializeUserBooksUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()