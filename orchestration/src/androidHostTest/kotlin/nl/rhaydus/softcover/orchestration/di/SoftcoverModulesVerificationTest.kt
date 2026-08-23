package nl.rhaydus.softcover.orchestration.di

import android.content.Context
import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.notification.NotificationAppearance
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover

@OptIn(KoinExperimentalAPI::class)
class SoftcoverModulesVerificationTest {
    @Test
    fun `softcover module graph resolves`() {
        module { includes(softcoverModules) }
            .verify(
                extraTypes = listOf(
                    Context::class,                // runtime androidContext()
                    AppVersionProvider::class,                     // supplied by :app appModule
                    NotificationAppearance::class,                 // supplied by :app appModule
                    DataStore::class,      // runtime DataStore instance, created outside Koin
                    CoroutineDispatcher::class, // platform dispatchers (Main/IO/Default) supplied at runtime
                    CoroutineScope::class,      // ApplicationScope wraps an inline-constructed CoroutineScope, not a Koin binding
                    BookInitialCover::class, // passed via parametersOf at BookDetailScreen call site
                    List::class,                // getAll() calls return a List assembled at runtime, never a bound type
                ),
            )
    }
}
