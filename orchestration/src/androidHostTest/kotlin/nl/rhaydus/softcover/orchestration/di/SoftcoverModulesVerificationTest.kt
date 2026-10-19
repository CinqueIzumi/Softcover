package nl.rhaydus.softcover.orchestration.di

import android.content.Context
import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.notification.NotificationAppearance
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class SoftcoverModulesVerificationTest {
    @Nested
    inner class Graph {
        @Test
        fun `softcover module graph resolves`() {
            // ----- Arrange -----
            val graph = module { includes(softcoverModules) }

            // ----- Act & Assert -----
            graph.verify(
                extraTypes = listOf(
                    // runtime androidContext()
                    Context::class,
                    // supplied by :app appModule
                    AppVersionProvider::class,
                    // supplied by :app appModule
                    NotificationAppearance::class,
                    // runtime DataStore instance, created outside Koin
                    DataStore::class,
                    // platform dispatchers (Main/IO/Default) supplied at runtime
                    CoroutineDispatcher::class,
                    // ApplicationScope wraps an inline-constructed CoroutineScope, not a Koin binding
                    CoroutineScope::class,
                    // passed via parametersOf at BookDetailScreen call site
                    BookInitialCover::class,
                    // passed via parametersOf at the document page screen call site
                    DocumentPage::class,
                    // passed via parametersOf at BarcodeScannerScreen call site
                    Boolean::class,
                    // getAll() calls return a List assembled at runtime, never a bound type
                    List::class,
                    // BackgroundWidgetRenderer's sources Map is assembled inline in the definition, never a bound type
                    Map::class,
                ),
            )
        }
    }
}
