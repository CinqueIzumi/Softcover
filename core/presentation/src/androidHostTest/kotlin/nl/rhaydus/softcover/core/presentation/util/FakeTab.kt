package nl.rhaydus.softcover.core.presentation.util

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

internal class FakeTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 0u,
            title = "fake",
        )

    @Composable
    override fun Content() = Unit
}
