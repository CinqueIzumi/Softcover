package nl.rhaydus.softcover

import android.app.Application
import nl.rhaydus.softcover.di.apolloModule
import nl.rhaydus.softcover.di.dataSourceModule
import nl.rhaydus.softcover.di.dataStoreModule
import nl.rhaydus.softcover.di.dispatcherModule
import nl.rhaydus.softcover.di.repositoryModule
import nl.rhaydus.softcover.di.screenModelModule
import nl.rhaydus.softcover.di.useCaseModule
import nl.rhaydus.softcover.di.utilModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class SoftCoverApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@SoftCoverApp)

            modules(
                apolloModule,
                dataSourceModule,
                dataStoreModule,
                dispatcherModule,
                repositoryModule,
                screenModelModule,
                useCaseModule,
                utilModule,
            )
        }
    }
}