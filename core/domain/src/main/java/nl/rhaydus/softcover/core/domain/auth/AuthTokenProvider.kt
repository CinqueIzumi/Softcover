package nl.rhaydus.softcover.core.domain.auth

import kotlinx.coroutines.flow.Flow

/**
 * Read-only access to the current auth credential, exposed as a contract in domain so the network
 * layer can attach the bearer token without depending on the preferences module (where the secure
 * storage impl lives). Inverts what would otherwise be a network ↔ preferences dependency cycle.
 */
interface AuthTokenProvider {
    val apiKey: Flow<String?>
}
