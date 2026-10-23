package nl.rhaydus.softcover.showcase

import android.app.Application
import nl.rhaydus.softcover.showcase.di.liveInPageModule
import nl.rhaydus.softcover.showcase.di.liveOverlaysModule
import nl.rhaydus.softcover.showcase.di.liveSheetsModule
import nl.rhaydus.softcover.showcase.di.showcaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ShowcaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ShowcaseApplication)
            modules(
                showcaseModule,
                liveSheetsModule,
                liveOverlaysModule,
                liveInPageModule,
            )
        }
    }
}
