package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource

object ReadingTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Reading"
            val icon = SoftcoverIconResource.Drawable(
                icon = SoftcoverIcon.Reading,
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
    override fun Content() = ReadingScreen.Content()
}
