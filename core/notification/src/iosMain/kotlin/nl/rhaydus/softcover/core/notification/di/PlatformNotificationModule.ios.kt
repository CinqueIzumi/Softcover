package nl.rhaydus.softcover.core.notification.di

import nl.rhaydus.softcover.core.notification.SoftcoverNotifier
import nl.rhaydus.softcover.core.notification.SoftcoverNotifierImpl
import org.koin.dsl.module

actual val platformNotificationModule = module {
    single<SoftcoverNotifier> { SoftcoverNotifierImpl() }
}
