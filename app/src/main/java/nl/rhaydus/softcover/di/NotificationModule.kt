package nl.rhaydus.softcover.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.notification.NotificationChannelInitializer
import nl.rhaydus.softcover.core.notification.SoftcoverNotifier
import nl.rhaydus.softcover.core.notification.SoftcoverNotifierImpl

val notificationModule = module {
    single<NotificationChannelInitializer> {
        NotificationChannelInitializer(context = androidContext())
    }

    single<SoftcoverNotifier> {
        SoftcoverNotifierImpl(
            context = androidContext(),
            smallIcon = R.drawable.ic_bookmark,
            accentColor = R.color.notification_accent,
        )
    }
}
