package nl.rhaydus.softcover.showcase.appearance.state

import nl.rhaydus.toad.UiState

/** A screen state that carries an [AppearanceState], so [S] can be updated without knowing the screen. */
internal interface AppearanceUiState<S : AppearanceUiState<S>> : UiState {
    val appearance: AppearanceState

    fun withAppearance(appearance: AppearanceState): S
}
