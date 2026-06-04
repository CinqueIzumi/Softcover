package nl.rhaydus.softcover.core.preferences.data.security

/**
 * Platform-backed secure storage for the API key. The key is the app's single sensitive secret, so
 * it is never persisted in the plain DataStore — each target keeps it in hardware-backed storage
 * (Android Keystore-encrypted file, iOS Keychain). Common code orchestrates loading/migration; this
 * interface is the only piece that differs per platform.
 */
internal interface SecureApiKeyStorage {
    suspend fun read(): String?

    suspend fun write(value: String)

    suspend fun delete()
}
