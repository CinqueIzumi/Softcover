package nl.rhaydus.softcover.core.network.di

import com.apollographql.apollo.network.http.DefaultHttpEngine
import com.apollographql.apollo.network.http.HttpEngine
import org.koin.dsl.module

// Shorter than apolloModule's 60s: the plain-HTTP seam fetches small static files, so a stuck
// connection should surface as a failure well before a GraphQL operation would give up.
private const val HTTP_TIMEOUT_MILLIS = 15_000L

/**
 * The engine behind [nl.rhaydus.softcover.core.network.helper.safeGetText]. Separate from
 * [apolloModule]'s client — which builds and owns (and closes) its own engine — so a plain-HTTP
 * caller never depends on the GraphQL client's lifetime.
 */
val httpModule = module {
    single<HttpEngine> {
        DefaultHttpEngine(timeoutMillis = HTTP_TIMEOUT_MILLIS)
    }
}
