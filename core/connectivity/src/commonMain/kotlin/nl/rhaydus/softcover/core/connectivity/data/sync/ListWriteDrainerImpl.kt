package nl.rhaydus.softcover.core.connectivity.data.sync

import nl.rhaydus.offlinesync.OfflineWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer

/** See [UserBookWriteDrainerImpl] — the same delegation, for the list drainer. */
internal class ListWriteDrainerImpl(
    delegate: OfflineWriteDrainer<Unit, Unit>,
) : ListWriteDrainer, OfflineWriteDrainer<Unit, Unit> by delegate
