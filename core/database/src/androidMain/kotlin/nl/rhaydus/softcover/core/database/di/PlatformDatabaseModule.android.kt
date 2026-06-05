package nl.rhaydus.softcover.core.database.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.domain.model.AppDispatchers

actual val platformDatabaseModule: Module = module {
    single<SoftcoverDatabase> {
        val context = androidContext()
        // Absolute path (not a bare name): the bundled SQLite driver opens the file directly and does
        // not resolve names through the Context, so we hand it the same databases-dir path the
        // reflection-based builder used — preserving the existing `books.db` store.
        val databasePath = context.getDatabasePath("books.db").absolutePath
        SoftcoverDatabase.build(
            builder = Room.databaseBuilder(
                context = context,
                name = databasePath,
            ),
            queryContext = get<AppDispatchers>().io,
        )
    }
}
