package nl.rhaydus.softcover.core.network.cache

import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.CompiledNamedType
import com.apollographql.apollo.api.CompiledType
import com.apollographql.apollo.api.Executable
import com.apollographql.apollo.api.Optional
import com.apollographql.cache.normalized.api.CacheHeaders
import com.apollographql.cache.normalized.api.CacheKey
import com.apollographql.cache.normalized.api.DefaultFieldKeyGenerator
import com.apollographql.cache.normalized.api.ResolverContext
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SoftcoverCacheResolverTest {
    private lateinit var field: CompiledField
    private lateinit var variables: Executable.Variables
    private lateinit var rawType: CompiledNamedType

    @BeforeEach
    fun setUp() {
        field = mockk()
        variables = mockk()
        rawType = mockk()

        val compiledType = mockk<CompiledType>()

        every {
            field.type
        } returns compiledType
        every {
            compiledType.rawType()
        } returns rawType
        every {
            field.responseName
        } returns "books"
    }

    private fun stubArgumentValue(value: Any?) {
        every {
            field.argumentValue(
                any(),
                any(),
            )
        } returns Optional.present(value)
    }

    private fun stubArgumentAbsent() {
        every {
            field.argumentValue(
                any(),
                any(),
            )
        } returns Optional.Absent
    }

    /**
     * Stubs the two CompiledField methods called by FieldPolicyCacheResolver / DefaultCacheResolver
     * when the resolver falls through to the default path:
     * - argumentValues(variables, filter) — FieldPolicyCacheResolver calls this to check for key args
     * - nameWithArguments(variables)       — DefaultCacheResolver uses this as the parent-map key
     *
     * By returning an empty map for argumentValues (no key args) and [responseName] for
     * nameWithArguments, DefaultCacheResolver looks up [responseName] in parent and returns whatever
     * sentinel value was stored there.
     */
    private fun stubForDelegation(responseName: String) {
        every {
            field.responseName
        } returns responseName
        every {
            field.argumentValues(
                any(),
                any(),
            )
        } returns emptyMap()
        every {
            field.nameWithArguments(any())
        } returns responseName
    }

    private fun resolve(
        name: String = "books",
        parentKey: CacheKey = CacheKey.QUERY_ROOT,
        parent: Map<String, Any?> = emptyMap(),
    ): Any? {
        every {
            field.name
        } returns name

        val context = ResolverContext(
            field = field,
            variables = variables,
            parent = parent,
            parentKey = parentKey,
            parentType = "Query",
            cacheHeaders = CacheHeaders.NONE,
            fieldKeyGenerator = DefaultFieldKeyGenerator,
            path = emptyList(),
        )

        return SoftcoverCacheResolver.resolveField(context)
    }

    @Nested
    inner class Redirects {
        @Test
        fun `QUERY_ROOT + books + where id _eq Int id produces single CacheKey with id converted to String`() {
            // ----- Arrange -----
            every {
                rawType.name
            } returns "books"
            stubArgumentValue(mapOf("id" to mapOf("_eq" to 123)))

            // ----- Act -----
            val result = resolve(name = "books")

            // ----- Assert -----
            result shouldBe listOf(
                CacheKey(
                    "books",
                    "123",
                ),
            )
        }

        @Test
        fun `QUERY_ROOT + editions + where id _in list filters nulls and returns CacheKey per id`() {
            // ----- Arrange -----
            every {
                rawType.name
            } returns "editions"
            stubArgumentValue(mapOf("id" to mapOf("_in" to listOf(1, null, 3))))

            // ----- Act -----
            val result = resolve(name = "editions")

            // ----- Assert -----
            result shouldBe listOf(
                CacheKey(
                    "editions",
                    "1",
                ),
                CacheKey(
                    "editions",
                    "3",
                ),
            )
        }

        @Test
        fun `QUERY_ROOT + authors + where id _eq String id produces CacheKey`() {
            // ----- Arrange -----
            every {
                rawType.name
            } returns "authors"
            stubArgumentValue(mapOf("id" to mapOf("_eq" to "42")))

            // ----- Act -----
            val result = resolve(name = "authors")

            // ----- Assert -----
            result shouldBe listOf(
                CacheKey(
                    "authors",
                    "42",
                ),
            )
        }

        @Test
        fun `QUERY_ROOT + user_books + where id _in empty list returns empty CacheKey list`() {
            // ----- Arrange -----
            every {
                rawType.name
            } returns "user_books"
            stubArgumentValue(mapOf("id" to mapOf("_in" to listOf<Int>())))

            // ----- Act -----
            val result = resolve(name = "user_books")

            // ----- Assert -----
            result shouldBe emptyList<CacheKey>()
        }
    }

    @Nested
    inner class Delegates {
        @Test
        fun `QUERY_ROOT + books + where has foreign key only (no id key) delegates to FieldPolicyCacheResolver`() {
            // ----- Arrange -----
            val sentinel = "sentinel-foreign-key"
            stubForDelegation("books")
            every {
                field.argumentValue(
                    any(),
                    any(),
                )
            } returns Optional.present(mapOf("book_id" to mapOf("_eq" to 5)))

            // ----- Act -----
            val result = resolve(
                name = "books",
                parent = mapOf("books" to sentinel),
            )

            // ----- Assert -----
            result shouldBe sentinel
        }

        @Test
        fun `QUERY_ROOT + books + no where argument delegates to FieldPolicyCacheResolver`() {
            // ----- Arrange -----
            val sentinel = "sentinel-no-where"
            stubForDelegation("books")
            stubArgumentAbsent()

            // ----- Act -----
            val result = resolve(
                name = "books",
                parent = mapOf("books" to sentinel),
            )

            // ----- Assert -----
            result shouldBe sentinel
        }

        @Test
        fun `QUERY_ROOT + non-redirectable field delegates without attempting redirect`() {
            // ----- Arrange -----
            val sentinel = "sentinel-non-redirectable"
            stubForDelegation("rating_distributions")

            // ----- Act -----
            val result = resolve(
                name = "rating_distributions",
                parent = mapOf("rating_distributions" to sentinel),
            )

            // ----- Assert -----
            result shouldBe sentinel
        }

        @Test
        fun `non-QUERY_ROOT parentKey + books in redirectable set still delegates`() {
            // ----- Arrange -----
            val sentinel = "sentinel-non-root"
            stubForDelegation("books")

            // ----- Act -----
            val result = resolve(
                name = "books",
                parentKey = CacheKey(
                    "books",
                    "1",
                ),
                parent = mapOf("books" to sentinel),
            )

            // ----- Assert -----
            result shouldBe sentinel
        }
    }
}
