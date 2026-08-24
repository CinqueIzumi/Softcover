package nl.rhaydus.softcover.core.network.interceptor

/**
 * The rate-limit budget the API grants the current credentials, as a token-bucket size plus the rate
 * it refills at. Modelled as a tier rather than two loose numbers so the *reason* a budget is what it
 * is stays attached to it — the numbers alone read as arbitrary tuning, and they are not: they mirror
 * limits the server enforces, and guessing them high means requests are refused rather than delayed.
 *
 * The refill rate is 1/sec on every tier the app can currently be on (the published per-minute limit
 * is 60 across plans); only the burst bucket differs. It is still carried per tier so a future tier
 * with a different refill does not have to reshape this type.
 *
 * The tier governs **only the first request of a process** — [RateLimitInterceptor] adopts the
 * server's own reported limit from every response after that. Issue #279 tracks selecting it at
 * runtime; see the Network Layer section of `docs/reference/architecture.md` for what a request costs.
 */
internal enum class ApiRateLimitTier(
    val bucketSize: Int,
    val refillTokensPerSecond: Double,
) {
    /**
     * Credentials still using the outdated JWT token format, which the API caps at a burst of 5.
     *
     * **This is the tier every install is currently on**, and what `apolloModule` binds. The token
     * migration has not run yet, so every signed-in user still holds an outdated token; assuming
     * anything wider would over-promise and produce the refusals the bucket exists to prevent.
     */
    LEGACY_JWT(
        bucketSize = 5,
        refillTokensPerSecond = 1.0,
    ),

    /**
     * The Hardcover Free plan: a burst of 10. Reachable only once the token migration has run and the
     * app can tell a migrated token from an outdated one — until then no install qualifies, so nothing
     * selects this tier.
     */
    FREE(
        bucketSize = 10,
        refillTokensPerSecond = 1.0,
    ),
}
