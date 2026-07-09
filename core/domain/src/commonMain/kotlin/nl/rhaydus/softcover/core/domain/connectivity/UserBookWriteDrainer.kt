package nl.rhaydus.softcover.core.domain.connectivity

import nl.rhaydus.offlinesync.OfflineWriteDrainer

/**
 * The foundation drainer, named for Softcover's user-book payload. `drain()` replays every queued offline
 * write and reports, per affected `userBook` id, which kinds of write were successfully synced to the
 * server. Callers use the kinds to preserve only the fields each replayed write actually owns when
 * reconciling against a fresh server fetch — a field the server changed underneath (and that no pending
 * write touched) must not be clobbered by the local optimistic copy.
 *
 * A named subinterface rather than a typealias: `OfflineWriteDrainer<Int, PendingUserBookWriteKind>` and
 * `OfflineWriteDrainer<Unit, Unit>` erase to the same class, so Koin could not tell the two drainers apart.
 */
interface UserBookWriteDrainer : OfflineWriteDrainer<Int, PendingUserBookWriteKind>
