package nl.rhaydus.softcover.feature.app_update.di

import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import nl.rhaydus.softcover.core.domain.appupdate.AppUpdateSimulator
import nl.rhaydus.softcover.feature.app_update.data.simulator.NoOpAppUpdateSimulator
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appUpdateVariantModule = module {
    single { AppUpdateManagerFactory.create(androidContext()) }

    single<AppUpdateSimulator> { NoOpAppUpdateSimulator }
}
