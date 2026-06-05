package nl.rhaydus.softcover.core.database.di

import androidx.room.Room
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.domain.model.AppDispatchers

actual val platformDatabaseModule: Module = module {
    single<SoftcoverDatabase> {
        SoftcoverDatabase.build(
            builder = Room.databaseBuilder(
                name = "${documentsDirectory()}/books.db",
            ),
            queryContext = get<AppDispatchers>().io,
        )
    }
}

private fun documentsDirectory(): String =
    NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).first() as String
