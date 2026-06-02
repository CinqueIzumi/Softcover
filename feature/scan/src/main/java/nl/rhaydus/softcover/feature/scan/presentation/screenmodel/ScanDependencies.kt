package nl.rhaydus.softcover.feature.scan.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.book.domain.usecase.AddBookByIsbnUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ResolveBookByIsbnUseCase
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionDependencies

internal data class ScanDependencies(
    val resolveBookByIsbnUseCase: ResolveBookByIsbnUseCase,
    val addBookByIsbnUseCase: AddBookByIsbnUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
