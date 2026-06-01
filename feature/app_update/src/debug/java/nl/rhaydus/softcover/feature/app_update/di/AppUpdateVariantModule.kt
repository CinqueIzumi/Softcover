package nl.rhaydus.softcover.feature.app_update.di

import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import nl.rhaydus.softcover.core.domain.appupdate.AppUpdateSimulator
import nl.rhaydus.softcover.feature.app_update.data.simulator.DebugAppUpdateSimulator
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appUpdateVariantModule = module {
    single { FakeAppUpdateManager(androidContext()) }

    single<AppUpdateManager> { get<FakeAppUpdateManager>() }

    single<AppUpdateSimulator> {
        DebugAppUpdateSimulator(
            fakeAppUpdateManager = get(),
            appUpdateDataSource = get(),
            checkForAppUpdateUseCase = get(),
        )
    }
}
