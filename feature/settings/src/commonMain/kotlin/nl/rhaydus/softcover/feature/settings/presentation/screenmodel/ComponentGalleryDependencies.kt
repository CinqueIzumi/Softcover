package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.toad.ActionDependencies

/**
 * [getThemeConfigurationUseCase] backs
 * [nl.rhaydus.softcover.feature.settings.presentation.collector.GalleryThemeConfigurationCollector],
 * which keeps [ComponentGalleryUiState][nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState]'s
 * mapped spine-colour fields in step with the app's own live theme configuration. No other use case
 * is involved — selecting a family or previewing an override only ever writes to the `UiState`
 * itself.
 */
internal class ComponentGalleryDependencies(
    val getThemeConfigurationUseCase: GetThemeConfigurationUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
