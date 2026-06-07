package nl.rhaydus.softcover.core.designsystem.presentation.toad

interface Initializer<S : UiState, E : UiEvent, D : ActionDependencies, V : LocalVariables> {
    suspend fun onLaunch(
        scope: ActionScope<S, E, V>,
        dependencies: D,
    )
}
