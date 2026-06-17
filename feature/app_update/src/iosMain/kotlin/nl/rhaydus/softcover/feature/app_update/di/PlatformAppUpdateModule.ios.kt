package nl.rhaydus.softcover.feature.app_update.di

import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.appupdate.AppUpdateSimulator
import nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateDataSource
import nl.rhaydus.softcover.feature.app_update.data.datasource.IosAppUpdateDataSource
import nl.rhaydus.softcover.feature.app_update.data.simulator.NoOpAppUpdateSimulator

// iOS has no in-app update flow, so the data source and simulator are both no-ops. There is no
// AppUpdateFlowLauncher binding — nothing on iOS starts an update flow. The simulator is bound (not
// omitted) so the settings debug section's koinInject<AppUpdateSimulator>() always resolves.
actual val platformAppUpdateModule: Module = module {
    single<AppUpdateDataSource> { IosAppUpdateDataSource() }

    single<AppUpdateSimulator> { NoOpAppUpdateSimulator }
}
