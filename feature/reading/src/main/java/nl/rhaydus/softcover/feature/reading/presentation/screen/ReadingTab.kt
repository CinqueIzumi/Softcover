package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import nl.rhaydus.softcover.core.designsystem.R

object ReadingTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Reading"
            val icon = painterResource(R.drawable.ic_reading)

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