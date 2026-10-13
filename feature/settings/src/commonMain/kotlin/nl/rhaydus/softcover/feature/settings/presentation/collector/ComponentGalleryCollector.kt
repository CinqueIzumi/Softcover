package nl.rhaydus.softcover.feature.settings.presentation.collector

import nl.rhaydus.softcover.feature.settings.presentation.event.ComponentGalleryEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
import nl.rhaydus.toad.Collector

/**
 * [GalleryRegistry][nl.rhaydus.softcover.core.component.gallery.GalleryRegistry] itself is static
 * data, not a flow, so the only implementation is
 * [GalleryThemeConfigurationCollector][nl.rhaydus.softcover.feature.settings.presentation.collector.GalleryThemeConfigurationCollector] —
 * it maps the app's live theme configuration into the gallery's own state, off the composition
 * (`component-contract.md` R9).
 */
internal sealed interface ComponentGalleryCollector : Collector<
        ComponentGalleryUiState,
        ComponentGalleryEvent,
        ComponentGalleryDependencies,
        ComponentGalleryLocalVariables,
        >
