package nl.rhaydus.softcover.feature.library.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

object LibraryTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Library"
            val icon = drawableIconResource(
                icon = SoftcoverIcon.Shelf,
                contentDescription = title,
            ).getIconPainter()

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon,
                )
            }
        }

    @Composable
    override fun Content() = LibraryScreen.Content()
}
