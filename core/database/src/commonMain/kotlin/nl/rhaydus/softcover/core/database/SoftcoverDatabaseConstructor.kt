package nl.rhaydus.softcover.core.database

import androidx.room.RoomDatabaseConstructor

/**
 * Room generates the `actual` for this per target — `@ConstructedBy` on [SoftcoverDatabase] points at
 * it, replacing the reflection-based instantiation the Android-only `Room.databaseBuilder(klass)` used.
 * The `NO_ACTUAL_FOR_EXPECT` suppression is required because the compiler cannot see Room's generated
 * `actual` when type-checking `commonMain`.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object SoftcoverDatabaseConstructor : RoomDatabaseConstructor<SoftcoverDatabase> {
    override fun initialize(): SoftcoverDatabase
}
