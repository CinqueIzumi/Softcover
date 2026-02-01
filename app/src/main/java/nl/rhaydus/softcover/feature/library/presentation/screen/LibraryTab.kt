package nl.rhaydus.softcover.feature.library.presentation.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object LibraryTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Library"
            val icon = rememberVectorPainter(image = Icons.Default.Bookmarks)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() = LibraryScreen.Content()
}