package nl.rhaydus.softcover.core.domain.connectivity

import nl.rhaydus.offlinesync.OfflineWriteDrainer

/** See [ListWriteDrainer]: quote writes need no reconciliation hints, so both type parameters are [Unit]. */
interface QuoteWriteDrainer : OfflineWriteDrainer<Unit, Unit>
