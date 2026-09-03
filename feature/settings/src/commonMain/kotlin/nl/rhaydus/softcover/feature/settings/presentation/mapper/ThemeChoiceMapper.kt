package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.designsystem.presentation.component.ThemeTilePainting
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.feature.settings.presentation.model.ThemeChoice

/**
 * Which of the light/dark pair each theme choice paints in its preview tile: the two explicit modes
 * paint themselves, and `SYSTEM` — which has no single colour — paints the diagonal split.
 *
 * Feature-local by R6: the Appearance screen is the only consumer, and it is the only place that
 * knows a `SYSTEM` choice reads as "whichever your device is". It is promoted to `:core:uibinding`
 * if a second surface ever needs the same reading.
 */
private fun ThemeMode.toTilePainting(): ThemeTilePainting = when (this) {
    ThemeMode.LIGHT -> ThemeTilePainting.LIGHT
    ThemeMode.DARK -> ThemeTilePainting.DARK
    ThemeMode.SYSTEM -> ThemeTilePainting.SPLIT
}

/**
 * One [ThemeChoice] per [ThemeMode], marking [selected] as the reader's chosen mode. Invoked off the
 * composition — in [nl.rhaydus.softcover.feature.settings.presentation.collector.ThemeConfigurationCollector]
 * and as [nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState]'s own
 * default — so the Appearance screen's render never calls [toTilePainting] itself
 * (`component-contract.md` R9).
 */
internal fun themeChoicesFor(selected: ThemeMode): ImmutableList<ThemeChoice> = ThemeMode.entries
    .map { mode ->
        ThemeChoice(
            label = mode.label,
            painting = mode.toTilePainting(),
            selected = mode == selected,
            mode = mode,
        )
    }
    .toImmutableList()
