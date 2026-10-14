package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.feature.settings.presentation.source.DocumentPageSource
import nl.rhaydus.toad.ActionDependencies

internal class DocumentPageDependencies(
    val source: DocumentPageSource,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
