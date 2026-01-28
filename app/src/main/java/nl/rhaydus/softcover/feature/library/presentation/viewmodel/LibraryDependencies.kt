package nl.rhaydus.softcover.feature.library.presentation.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.library.domain.usecase.GetUserBooksAsFlowUseCase

class LibraryDependencies(
    val getUserBooksAsFlowUseCase: GetUserBooksAsFlowUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()