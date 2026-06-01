package nl.rhaydus.softcover.feature.session.di

import nl.rhaydus.softcover.core.presentation.session.ReadingSessionLauncher
import nl.rhaydus.softcover.feature.session.presentation.service.ReadingSessionLauncherImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val sessionModule = module {
    single<ReadingSessionLauncher> {
        ReadingSessionLauncherImpl(context = androidContext())
    }
}
