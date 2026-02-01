package nl.rhaydus.softcover.feature.search.presentation.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.GetPreviousSearchQueriesUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.GetQueriedBooksUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveAllSearchQueriesUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveSearchQueryUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.SearchForNameUseCase

data class SearchDependencies(
    val getPreviousSearchQueriesUseCase: GetPreviousSearchQueriesUseCase,
    val getQueriedBooksUseCase: GetQueriedBooksUseCase,
    val searchForNameUseCase: SearchForNameUseCase,
    val removeSearchQueryUseCase: RemoveSearchQueryUseCase,
    val removeAllSearchQueriesUseCase: RemoveAllSearchQueriesUseCase,
    val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()