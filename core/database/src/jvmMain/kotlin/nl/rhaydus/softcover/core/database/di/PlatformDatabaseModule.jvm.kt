package nl.rhaydus.softcover.core.database.di

import androidx.room.Room
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.domain.platform.desktopAppDataDirectory
import nl.rhaydus.ui.common.AppDispatchers

// Room generates the SoftcoverDatabaseConstructor actual for the JVM target via kspJvm; only the
// builder's on-disk location is platform-bound. The migrations, bundled SQLite driver, and query
// context are shared via SoftcoverDatabase.build.
actual val platformDatabaseModule: Module = module {
    single<SoftcoverDatabase> {
        SoftcoverDatabase.build(
            builder = Room.databaseBuilder(
                name = "${desktopAppDataDirectory()}/books.db",
            ),
            queryContext = get<AppDispatchers>().io,
        )
    }
}
