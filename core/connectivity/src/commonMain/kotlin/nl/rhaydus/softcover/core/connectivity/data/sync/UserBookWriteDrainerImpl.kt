package nl.rhaydus.softcover.core.connectivity.data.sync

import nl.rhaydus.offlinesync.OfflineWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteDrainer

/**
 * Gives the foundation's `DefaultOfflineWriteDrainer` — a class, not an interface — Softcover's named
 * drainer type, by delegation. The name is what lets Koin bind the two drainers separately: their generic
 * foundation supertypes erase to one class. See [UserBookWriteDrainer].
 */
internal class UserBookWriteDrainerImpl(
    delegate: OfflineWriteDrainer<Int, PendingUserBookWriteKind>,
) : UserBookWriteDrainer, OfflineWriteDrainer<Int, PendingUserBookWriteKind> by delegate
