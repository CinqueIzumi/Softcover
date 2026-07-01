package nl.rhaydus.softcover.feature.app_update.di

import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.appupdate.AppUpdateSimulator
import nl.rhaydus.softcover.core.domain.platform.desktopAppDataDirectory
import nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateDataSource
import nl.rhaydus.softcover.feature.app_update.data.datasource.JvmAppUpdateDataSource
import nl.rhaydus.softcover.feature.app_update.data.install.DesktopInstallerLauncher
import nl.rhaydus.softcover.feature.app_update.data.release.GitHubReleaseSource
import nl.rhaydus.softcover.feature.app_update.data.release.ReleaseSource
import nl.rhaydus.softcover.feature.app_update.data.simulator.NoOpAppUpdateSimulator
import nl.rhaydus.softcover.feature.app_update.domain.launcher.AppUpdateFlowLauncher
import nl.rhaydus.softcover.feature.app_update.domain.launcher.JvmAppUpdateFlowLauncher

// Desktop drives a GitHub-releases-backed self-updater. The provider (GitHubReleaseSource) sits
// behind the provider-agnostic ReleaseSource, which the JvmAppUpdateDataSource state machine depends
// on. The concrete data source is exposed as both the shared AppUpdateDataSource and the target the
// AppUpdateFlowLauncher closes over to start the download. The simulator stays a no-op (the settings
// debug section only drives the fake Play flow on Android).
actual val platformAppUpdateModule: Module = module {
    single<ReleaseSource> { GitHubReleaseSource(appDispatchers = get()) }

    single {
        JvmAppUpdateDataSource(
            appVersionProvider = get(),
            releaseSource = get(),
            installerLauncher = DesktopInstallerLauncher(),
            appDispatchers = get(),
            appDataDirectory = desktopAppDataDirectory(),
        )
    }

    single<AppUpdateDataSource> { get<JvmAppUpdateDataSource>() }

    single<AppUpdateFlowLauncher> {
        JvmAppUpdateFlowLauncher(appUpdateDataSource = get<JvmAppUpdateDataSource>())
    }

    single<AppUpdateSimulator> { NoOpAppUpdateSimulator }
}
