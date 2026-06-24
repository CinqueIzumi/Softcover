package nl.rhaydus.softcover.feature.session.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.notification.di.notificationModule

val sessionModule = module {
    includes(
        platformSessionModule,
        designSystemModule,
        notificationModule,
    )
}
