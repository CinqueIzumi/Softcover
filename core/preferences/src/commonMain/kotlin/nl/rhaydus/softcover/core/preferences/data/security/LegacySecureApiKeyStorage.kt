package nl.rhaydus.softcover.core.preferences.data.security

/**
 * Read-and-delete access to the API key's **pre-foundation** secure location, so an upgrading install can
 * carry the key forward into `nl.rhaydus.platform.SecureStorage` instead of bouncing the user to the login
 * screen. Adopting the foundation store relocated the secret on two platforms:
 *
 * - **Android** — the ciphertext moved from `api_key.enc` to `secure_api_95_key.enc`, and (decisively) it
 *   is encrypted under a *different* Keystore key: alias `softcover_api_key` before, `rhaydus_secure_storage`
 *   after. The old bytes cannot be decrypted by the new store even if it found them.
 * - **iOS** — the Keychain item is addressed by `(service, account)`, and both halves changed:
 *   `("nl.rhaydus.softcover", "softcover_api_key")` → `("nl.rhaydus.secure_storage", "api_key")`.
 *
 * Desktop needs no migration: the app injects its own namespaced `KSafe` and the key string `"api_key"` is
 * unchanged, so there is no jvm implementation of this interface.
 *
 * **Temporary.** Once every install has passed through a build carrying this migration, delete this
 * interface and its two implementations — the migration is one-shot and idempotent, and a user who skips
 * the release simply re-authenticates. Tracked in `docs/working/now.md`.
 */
internal interface LegacySecureApiKeyStorage {
    suspend fun read(): String?

    suspend fun delete()
}
