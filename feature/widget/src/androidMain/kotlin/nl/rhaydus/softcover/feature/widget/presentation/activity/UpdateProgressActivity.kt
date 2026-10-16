package nl.rhaydus.softcover.feature.widget.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cafe.adriel.voyager.navigator.Navigator
import nl.rhaydus.softcover.feature.widget.presentation.screen.UpdateProgressScreen

internal class UpdateProgressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val bookId = intent.getIntExtra(
            EXTRA_BOOK_ID,
            INVALID_BOOK_ID,
        )

        if (bookId == INVALID_BOOK_ID) {
            finish()

            return
        }

        setContent {
            Navigator(UpdateProgressScreen(bookId = bookId))
        }
    }

    private companion object {
        const val INVALID_BOOK_ID = -1
    }
}
