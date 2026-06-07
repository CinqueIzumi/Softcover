package nl.rhaydus.softcover.core.designsystem.presentation.toad

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class ActionDependencies {
    abstract val coroutineScope: CoroutineScope
    abstract val mainDispatcher: CoroutineDispatcher

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch(
            context = mainDispatcher,
            block = block,
        )
    }
}
