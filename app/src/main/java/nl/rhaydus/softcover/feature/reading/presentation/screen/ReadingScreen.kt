package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

object ReadingScreen : Screen {
    @Composable
    override fun Content() {
        Text("Reading screen")
    }
}