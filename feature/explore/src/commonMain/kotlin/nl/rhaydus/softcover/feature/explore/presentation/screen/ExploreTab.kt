package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object ExploreTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Explore"

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                )
            }
        }

    @Composable
    override fun Content() = ExploreScreen.Content()
}
