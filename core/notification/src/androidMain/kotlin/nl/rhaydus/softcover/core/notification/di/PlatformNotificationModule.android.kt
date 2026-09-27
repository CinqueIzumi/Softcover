package nl.rhaydus.softcover.core.notification.di

import nl.rhaydus.softcover.core.notification.NotificationAppearance
import nl.rhaydus.softcover.core.notification.NotificationChannelInitializer
import nl.rhaydus.softcover.core.notification.SoftcoverNotifier
import nl.rhaydus.softcover.core.notification.SoftcoverNotifierImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformNotificationModule = module {
    single<NotificationChannelInitializer> {
        NotificationChannelInitializer(context = androidContext())
    }

    single<SoftcoverNotifier> {
        SoftcoverNotifierImpl(
            context = androidContext(),
            appearance = get<NotificationAppearance>(),
        )
    }
}
