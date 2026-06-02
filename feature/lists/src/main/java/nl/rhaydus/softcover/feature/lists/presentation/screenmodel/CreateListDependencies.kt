package nl.rhaydus.softcover.feature.lists.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.lists.domain.usecase.CreateListUseCase

internal data class CreateListDependencies(
    val createListUseCase: CreateListUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
