package nl.rhaydus.softcover.core.domain.model

import kotlinx.coroutines.CoroutineDispatcher

data class AppDispatchers(
    val main: CoroutineDispatcher,
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
)