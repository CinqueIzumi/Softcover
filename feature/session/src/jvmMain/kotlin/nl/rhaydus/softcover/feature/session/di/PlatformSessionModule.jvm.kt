package nl.rhaydus.softcover.feature.session.di

import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.presentation.session.ReadingSessionLauncher

// Desktop has no foreground-service equivalent for the lock-screen reading surface. The in-app
// session UI (peek bar + Focus Mode) still works; only the OS-level surface is absent, so the
// launcher is a no-op. Bound (rather than left unbound) so ActiveSessionController always resolves.
actual val platformSessionModule: Module = module {
    single<ReadingSessionLauncher> {
        object : ReadingSessionLauncher {
            override fun start() {
                AppLog.i("Reading-session lock-screen surface not available on this platform yet.")
            }
        }
    }
}
