package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.AppVersionProviderImpl
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.notification.NotificationAccentColor
import nl.rhaydus.softcover.core.notification.NotificationAppearance
import nl.rhaydus.softcover.core.notification.NotificationIcon
import org.koin.dsl.module

internal val appModule = module {
    single<AppVersionProvider> { AppVersionProviderImpl() }

    single {
        NotificationAppearance(
            smallIcon = NotificationIcon(R.drawable.ic_bookmark),
            accentColor = NotificationAccentColor(R.color.notification_accent),
        )
    }
}
