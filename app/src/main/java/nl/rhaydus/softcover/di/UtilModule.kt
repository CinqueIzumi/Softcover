package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress
import org.koin.dsl.module

val utilModule = module {
    factory { UpdateBookProgress(get(), get()) }
}