package nl.rhaydus.softcover.core.domain.connectivity

import nl.rhaydus.offlinesync.OfflineWriteDrainer

/**
 * The foundation drainer, named for Softcover's list payload. List writes need no reconciliation hints —
 * the server response is cached directly by each replay — so both hint type parameters are [Unit] and
 * `drain()` always returns an empty map. See [UserBookWriteDrainer] for why this is a named subinterface.
 */
interface ListWriteDrainer : OfflineWriteDrainer<Unit, Unit>
