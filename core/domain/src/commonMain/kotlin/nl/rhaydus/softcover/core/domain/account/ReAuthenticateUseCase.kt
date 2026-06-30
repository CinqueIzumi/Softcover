package nl.rhaydus.softcover.core.domain.account

/**
 * Re-authenticates with a freshly entered [apiKey] without the destructive logout: local data is
 * preserved when the key resolves to the same account, and only wiped (then reloaded) when it belongs
 * to a different account. Fails when the key is rejected by the server, leaving existing data intact.
 */
interface ReAuthenticateUseCase {
    suspend operator fun invoke(apiKey: String): Result<Unit>
}
