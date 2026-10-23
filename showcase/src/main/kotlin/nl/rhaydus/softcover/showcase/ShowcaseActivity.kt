package nl.rhaydus.softcover.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import nl.rhaydus.softcover.core.designsystem.presentation.systembar.transparentSystemBarStyle
import nl.rhaydus.softcover.showcase.screen.ShowcaseRoot

class ShowcaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = transparentSystemBarStyle(darkTheme = null),
            navigationBarStyle = transparentSystemBarStyle(darkTheme = null),
        )
        super.onCreate(savedInstanceState)

        setContent { ShowcaseRoot() }
    }
}
