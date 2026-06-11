package nl.rhaydus.softcover.feature.app_update.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.appupdate.AppUpdateSimulator
import nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateDataSource
import nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateDataSourceImpl
import nl.rhaydus.softcover.feature.app_update.data.simulator.DebugAppUpdateSimulator
import nl.rhaydus.softcover.feature.app_update.data.simulator.NoOpAppUpdateSimulator
import nl.rhaydus.softcover.feature.app_update.domain.launcher.AndroidAppUpdateFlowLauncher
import nl.rhaydus.softcover.feature.app_update.domain.launcher.AppUpdateFlowLauncher

// A KMP library produces a single Android variant, so the debug/release split is a runtime decision
// rather than a build-type source set: debuggable builds get a FakeAppUpdateManager driven by the
// in-app DebugAppUpdateSimulator, signed release builds get the real Play manager and a no-op
// simulator. FLAG_DEBUGGABLE is set by AGP per build type and cannot be flipped without re-signing,
// so the developer tooling is unreachable in release without leaning on build-type source sets (and
// keeps this self-contained — neither :orchestration nor :app needs to wire it).
actual val platformAppUpdateModule: Module = module {
    single<AppUpdateManager> {
        val context = androidContext()
        if (context.isDebuggable()) {
            FakeAppUpdateManager(context)
        } else {
            AppUpdateManagerFactory.create(context)
        }
    }

    // Bound as its concrete type so DebugAppUpdateSimulator can reach forceState() (not on the
    // interface); the interface alias below resolves to the same instance.
    single {
        AppUpdateDataSourceImpl(appUpdateManager = get())
    }

    single<AppUpdateDataSource> { get<AppUpdateDataSourceImpl>() }

    factory<AppUpdateFlowLauncher> { (launcher: ActivityResultLauncher<IntentSenderRequest>) ->
        AndroidAppUpdateFlowLauncher(
            appUpdateManager = get(),
            activityResultLauncher = launcher,
        )
    }

    single<AppUpdateSimulator> {
        when (val manager = get<AppUpdateManager>()) {
            is FakeAppUpdateManager -> DebugAppUpdateSimulator(
                fakeAppUpdateManager = manager,
                appUpdateDataSource = get(),
                checkForAppUpdateUseCase = get(),
            )

            else -> NoOpAppUpdateSimulator
        }
    }
}

private fun Context.isDebuggable(): Boolean =
    (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
