package nl.rhaydus.softcover.di

import org.koin.dsl.module
import nl.rhaydus.softcover.AppVersionProviderImpl
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.notification.NotificationAppearance

internal val appModule = module {
    single<AppVersionProvider> { AppVersionProviderImpl() }

    single {
        NotificationAppearance(
            smallIcon = R.drawable.ic_bookmark,
            accentColor = R.color.notification_accent,
        )
    }
}
