package nl.rhaydus.softcover.core.data.network.cache

import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.Executable
import com.apollographql.apollo.cache.normalized.api.CacheKey
import com.apollographql.apollo.cache.normalized.api.CacheResolver
import com.apollographql.apollo.cache.normalized.api.FieldPolicyCacheResolver

// Redirects Hasura-style root list queries filtered by primary key to the
// normalized cache entries written by other queries. Without this, fetching a
// book detail right after seeing it in a list still misses cache because
// ROOT_QUERY does not contain an entry for that specific field+args
// combination, even though the Book object itself is cached.
//
// Only safe for filters that uniquely identify rows (id._eq / id._in). Filters
// by foreign key (e.g. editions.where.book_id) can return partial lists, so
// they fall through to the default resolver and incur a network call.
object SoftcoverCacheResolver : CacheResolver {
    private const val ROOT_QUERY = "QUERY_ROOT"

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

    override fun resolveField(
        field: CompiledField,
        variables: Executable.Variables,
        parent: Map<String, Any?>,
        parentId: String,
    ): Any? {
        if (parentId == ROOT_QUERY && field.name in redirectableFields) {
            resolveById(field = field, variables = variables)?.let { return it }
        }

        return FieldPolicyCacheResolver.resolveField(
            field = field,
            variables = variables,
            parent = parent,
            parentId = parentId,
        )
    }

    private fun resolveById(
        field: CompiledField,
        variables: Executable.Variables,
    ): List<CacheKey>? {
        val where = field.argumentValue(name = "where", variables = variables).getOrNull() as? Map<*, *>
            ?: return null

        val idArg = where["id"] as? Map<*, *> ?: return null

        val typeName = field.type.rawType().name

        idArg["_eq"]?.let { id ->
            return listOf(CacheKey(typeName, id.toString()))
        }

        (idArg["_in"] as? List<*>)?.let { ids ->
            return ids.filterNotNull().map { id -> CacheKey(typeName, id.toString()) }
        }

        return null
    }
}
