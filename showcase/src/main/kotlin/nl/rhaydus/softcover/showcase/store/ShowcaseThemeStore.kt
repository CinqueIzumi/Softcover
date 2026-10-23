package nl.rhaydus.softcover.showcase.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette

/** The one look the whole showcase wears; the root reads it, the appearance sheet writes it. */
internal class ShowcaseThemeStore {
    private val mutableTheme = MutableStateFlow(ShowcaseTheme())

    val theme: StateFlow<ShowcaseTheme> = mutableTheme.asStateFlow()

    fun selectPalette(palette: SpinePalette) = mutableTheme.update { it.copy(palette = palette) }

    fun selectBrightness(brightness: SchemeBrightness) = mutableTheme.update { it.copy(brightness = brightness) }
}
