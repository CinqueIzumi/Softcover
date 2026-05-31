package nl.rhaydus.softcover.feature.scan.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.books.domain.usecase.ResolveBookByIsbnUseCase

data class ScanDependencies(
    val resolveBookByIsbnUseCase: ResolveBookByIsbnUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
