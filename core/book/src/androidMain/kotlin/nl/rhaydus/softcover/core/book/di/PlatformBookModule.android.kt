package nl.rhaydus.softcover.core.book.di

import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.book.data.storage.EditionImageStorage
import nl.rhaydus.softcover.core.book.data.storage.EditionImageStorageImpl

actual val platformBookModule: Module = module {
    single<EditionImageStorage> {
        EditionImageStorageImpl(
            fileSystem = FileSystem.SYSTEM,
            rootDir = androidContext().filesDir.path.toPath(),
        )
    }
}
