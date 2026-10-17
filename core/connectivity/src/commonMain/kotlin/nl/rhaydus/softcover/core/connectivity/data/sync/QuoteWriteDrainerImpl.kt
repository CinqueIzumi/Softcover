package nl.rhaydus.softcover.core.connectivity.data.sync

import nl.rhaydus.offlinesync.OfflineWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.QuoteWriteDrainer

/** See [UserBookWriteDrainerImpl]: the same delegation, for the quote drainer. */
internal class QuoteWriteDrainerImpl(
    delegate: OfflineWriteDrainer<Unit, Unit>,
) : QuoteWriteDrainer, OfflineWriteDrainer<Unit, Unit> by delegate
