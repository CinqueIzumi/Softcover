package nl.rhaydus.softcover.showcase.live.chrome.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.showcase.appearance.screenmodel.AppearanceDependencies
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.ActionDependencies

internal class ChromeLiveDependencies(
    override val themeStore: ShowcaseThemeStore,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies(), AppearanceDependencies
