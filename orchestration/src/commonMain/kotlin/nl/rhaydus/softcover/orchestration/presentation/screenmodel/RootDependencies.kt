package nl.rhaydus.softcover.orchestration.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.platform.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.component.state.OfflineCopy
import nl.rhaydus.toad.ActionDependencies

internal data class RootDependencies(
    val networkAvailabilityProvider: NetworkAvailabilityProvider,
    val offlineCopy: OfflineCopy,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
