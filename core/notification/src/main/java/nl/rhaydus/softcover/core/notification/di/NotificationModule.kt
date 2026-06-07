package nl.rhaydus.softcover.core.notification.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import nl.rhaydus.softcover.core.notification.NotificationAppearance
import nl.rhaydus.softcover.core.notification.NotificationChannelInitializer
import nl.rhaydus.softcover.core.notification.SoftcoverNotifier
import nl.rhaydus.softcover.core.notification.SoftcoverNotifierImpl

val notificationModule = module {
    single<NotificationChannelInitializer> {
        NotificationChannelInitializer(context = androidContext())
    }

    single<SoftcoverNotifier> {
        val appearance = get<NotificationAppearance>()

        SoftcoverNotifierImpl(
            context = androidContext(),
            smallIcon = appearance.smallIcon,
            accentColor = appearance.accentColor,
        )
    }
}
