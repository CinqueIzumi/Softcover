package nl.rhaydus.softcover.core.network.cache

import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.Executable
import com.apollographql.cache.normalized.api.CacheKey
import com.apollographql.cache.normalized.api.CacheResolver
import com.apollographql.cache.normalized.api.FieldPolicyCacheResolver
import com.apollographql.cache.normalized.api.ResolverContext
import nl.rhaydus.softcover.cache.Cache

// Redirects Hasura-style root list queries filtered by primary key to the
// normalized cache entries written by other queries. Without this, fetching a
// book detail right after seeing it in a list still misses cache because
// ROOT_QUERY does not contain an entry for that specific field+args
// combination, even though the Book object itself is cached.
//
// Only safe for filters that uniquely identify rows (id._eq / id._in). Filters
// by foreign key (e.g. editions.where.book_id) can return partial lists, so
// they fall through to the default resolver and incur a network call.
internal object SoftcoverCacheResolver : CacheResolver {
    // Field-policy-aware fallthrough resolver (the generated @fieldPolicy directives, plus the default
    // parent-map lookup for everything else) — the new-library equivalent of the old singleton.
    private val fallthrough = FieldPolicyCacheResolver(fieldPolicies = Cache.fieldPolicies)

    private val redirectableFields = setOf(
        "books",
        "editions",
        "authors",
        "users",
        "lists",
        "list_books",
        "user_books",
        "user_book_reads",
        "series",
    )

    override fun resolveField(context: ResolverContext): Any? {
        if (context.parentKey == CacheKey.QUERY_ROOT && context.field.name in redirectableFields) {
            resolveById(
                field = context.field,
                variables = context.variables,
            )?.let { return it }
        }

        return fallthrough.resolveField(context)
    }

    private fun resolveById(
        field: CompiledField,
        variables: Executable.Variables,
    ): List<CacheKey>? {
        val where = field.argumentValue(
            name = "where",
            variables = variables,
        ).getOrNull() as? Map<*, *>
            ?: return null

        val idArg = where["id"] as? Map<*, *> ?: return null

        val typeName = field.type.rawType().name

        idArg["_eq"]?.let { id ->
            return listOf(CacheKey(
                typeName,
                id.toString(),
            ),)
        }

        (idArg["_in"] as? List<*>)?.let { ids ->
            return ids.filterNotNull().map { id -> CacheKey(
                typeName,
                id.toString(),
            ) }
        }

        return null
    }
}
