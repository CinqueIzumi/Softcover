package nl.rhaydus.softcover.orchestration.di

import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.notification.NotificationAppearance
import org.junit.jupiter.api.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class SoftcoverModulesVerificationTest {
    @Test
    fun `softcover module graph resolves`() {
        module { includes(softcoverModules) }
            .verify(
                extraTypes = listOf(
                    android.content.Context::class,                // runtime androidContext()
                    AppVersionProvider::class,                     // supplied by :app appModule
                    NotificationAppearance::class,                 // supplied by :app appModule
                    androidx.datastore.core.DataStore::class,      // runtime DataStore instance, created outside Koin
                    kotlinx.coroutines.CoroutineDispatcher::class, // platform dispatchers (Main/IO/Default) supplied at runtime
                    kotlinx.coroutines.CoroutineScope::class,      // ApplicationScope wraps an inline-constructed CoroutineScope, not a Koin binding
                    nl.rhaydus.softcover.core.designsystem.presentation.model.BookInitialCover::class, // passed via parametersOf at BookDetailScreen call site
                    kotlin.collections.List::class,                // getAll() calls return a List assembled at runtime, never a bound type
                ),
            )
    }
}
