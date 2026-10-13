package nl.rhaydus.softcover.core.domain.di

import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCaseImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val currentDateModule = module {
    factory { ObserveCurrentDateUseCaseImpl() } bind ObserveCurrentDateUseCase::class
}
