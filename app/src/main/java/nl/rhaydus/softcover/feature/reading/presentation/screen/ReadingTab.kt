package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object ReadingTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Reading"
            val icon = rememberVectorPainter(image = Icons.AutoMirrored.Filled.MenuBook)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() = ReadingScreen.Content()
}