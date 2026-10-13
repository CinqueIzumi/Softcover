package nl.rhaydus.softcover.feature.session.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.toad.ActionDependencies

internal data class FocusModeDependencies(
    val activeSessionController: ActiveSessionController,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
