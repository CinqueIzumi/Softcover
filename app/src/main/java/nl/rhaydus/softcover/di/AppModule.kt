package nl.rhaydus.softcover.di

import org.koin.dsl.module
import nl.rhaydus.softcover.AppVersionProviderImpl
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider

internal val appModule = module {
    single<AppVersionProvider> { AppVersionProviderImpl() }
}
