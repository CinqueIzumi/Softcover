package nl.rhaydus.softcover.feature.session.di

import org.koin.dsl.module

val sessionModule = module {
    includes(platformSessionModule)
}
