package nl.rhaydus.softcover.core.notification.di

import nl.rhaydus.softcover.core.notification.JvmSoftcoverNotifier
import nl.rhaydus.softcover.core.notification.SoftcoverNotifier
import org.koin.dsl.module

actual val platformNotificationModule = module {
    single<SoftcoverNotifier> { JvmSoftcoverNotifier() }
}
