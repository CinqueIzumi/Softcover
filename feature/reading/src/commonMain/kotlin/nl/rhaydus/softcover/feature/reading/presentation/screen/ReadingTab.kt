package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object ReadingTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Reading"

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                )
            }
        }

    @Composable
    override fun Content() = ReadingScreen.Content()
}
