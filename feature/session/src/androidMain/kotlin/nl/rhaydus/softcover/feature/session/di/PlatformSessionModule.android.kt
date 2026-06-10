package nl.rhaydus.softcover.feature.session.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.presentation.session.ReadingSessionLauncher
import nl.rhaydus.softcover.feature.session.presentation.service.ReadingSessionLauncherImpl

actual val platformSessionModule: Module = module {
    single<ReadingSessionLauncher> {
        ReadingSessionLauncherImpl(context = androidContext())
    }
}
