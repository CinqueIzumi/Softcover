package nl.rhaydus.softcover.core.database.di

import androidx.room.Room
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.SoftcoverDatabaseConstructor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDatabaseModule: Module = module {
    single<SoftcoverDatabase> {
        val context = androidContext()
        // Absolute path (not a bare name): the bundled SQLite driver opens the file directly and does
        // not resolve names through the Context, so we hand it the same databases-dir path the
        // previous builder used — preserving the existing `books.db` store.
        val databasePath = context.getDatabasePath("books.db").absolutePath
        SoftcoverDatabase.build(
            builder = Room.databaseBuilder(
                context = context,
                name = databasePath,
                // Room's default factory finds `SoftcoverDatabase_Impl` by reflection; the generated
                // constructor is a direct reference instead. Room's shipped RoomDatabase keep rule
                // remains the safety net.
                factory = { SoftcoverDatabaseConstructor.initialize() },
            ),
            queryContext = get<AppDispatchers>().io,
        )
    }
}
