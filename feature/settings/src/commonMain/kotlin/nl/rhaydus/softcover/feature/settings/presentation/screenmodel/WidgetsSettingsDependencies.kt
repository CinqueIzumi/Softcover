package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgets
import nl.rhaydus.toad.ActionDependencies

internal class WidgetsSettingsDependencies(
    val homeScreenWidgets: HomeScreenWidgets,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
