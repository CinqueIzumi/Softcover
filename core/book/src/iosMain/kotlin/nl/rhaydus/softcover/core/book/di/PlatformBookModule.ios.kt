package nl.rhaydus.softcover.core.book.di

import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import nl.rhaydus.softcover.core.book.data.storage.EditionImageStorage
import nl.rhaydus.softcover.core.book.data.storage.EditionImageStorageImpl

actual val platformBookModule: Module = module {
    single<EditionImageStorage> {
        EditionImageStorageImpl(
            fileSystem = FileSystem.SYSTEM,
            rootDir = documentsDirectory().toPath(),
        )
    }
}

private fun documentsDirectory(): String =
    NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).first() as String
