package nl.rhaydus.softcover.feature.settings.presentation.collector

import nl.rhaydus.softcover.feature.settings.presentation.event.ComponentGalleryEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
import nl.rhaydus.toad.Collector

/**
 * The gallery observes nothing — [GalleryRegistry][nl.rhaydus.softcover.core.component.gallery.GalleryRegistry]
 * is static data, not a flow — so this sealed type never gains an implementation. It exists only so
 * [nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryScreenModel] can
 * pass `initializers = emptyList()` with a concrete `Collector` type parameter.
 */
internal sealed interface ComponentGalleryCollector : Collector<
        ComponentGalleryUiState,
        ComponentGalleryEvent,
        ComponentGalleryDependencies,
        ComponentGalleryLocalVariables,
        >
