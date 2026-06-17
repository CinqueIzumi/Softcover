package nl.rhaydus.softcover.feature.profile.di

import nl.rhaydus.softcover.feature.profile.presentation.collector.ProfileCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.UserInformationCollector
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val profileScreenModule = module {
    factory {
        ProfileScreenScreenModel(
            dispatchers = get(),
            initializers = getAll(),
            resetUserDataUseCase = get(),
            observeUserProfileDataUseCase = get(),
            refreshUserProfileDataUseCase = get(),
        )
    }

    factory { UserInformationCollector() } bind ProfileCollector::class
}
