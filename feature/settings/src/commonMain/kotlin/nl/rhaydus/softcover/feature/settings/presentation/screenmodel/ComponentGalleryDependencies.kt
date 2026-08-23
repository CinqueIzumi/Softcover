package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.toad.ActionDependencies

/**
 * The gallery's actions only ever write to [ComponentGalleryUiState][nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState]
 * — no use case is involved in selecting a family or previewing a theme override — so there is
 * nothing beyond the base [ActionDependencies] seams.
 */
internal class ComponentGalleryDependencies(
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
