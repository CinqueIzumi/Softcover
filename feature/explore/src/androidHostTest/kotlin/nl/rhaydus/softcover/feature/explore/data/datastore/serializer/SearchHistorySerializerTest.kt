package nl.rhaydus.softcover.feature.explore.data.datastore.serializer

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import okio.Buffer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SearchHistorySerializerTest {
    @Nested
    inner class DefaultValue {
        @Test
        fun `returns SearchHistoryEntity with empty previousQueries list`() {
            // ----- Arrange -----
            // (no additional setup)

            // ----- Act -----
            val result = SearchHistorySerializer.defaultValue

            // ----- Assert -----
            result shouldBe SearchHistoryEntity(previousQueries = emptyList())
        }
    }

    @Nested
    inner class ReadFrom {
        @Test
        fun `deserializes valid JSON with a list of queries`() = runTest {
            // ----- Arrange -----
            val json = """{"previousQueries":["kotlin","android","jetpack"]}"""
            val source = Buffer().writeUtf8(json)

            // ----- Act -----
            val result = SearchHistorySerializer.readFrom(source)

            // ----- Assert -----
            result shouldBe SearchHistoryEntity(previousQueries = listOf("kotlin", "android", "jetpack"))
        }

        @Test
        fun `deserializes valid JSON with an empty list`() = runTest {
            // ----- Arrange -----
            val json = """{"previousQueries":[]}"""
            val source = Buffer().writeUtf8(json)

            // ----- Act -----
            val result = SearchHistorySerializer.readFrom(source)

            // ----- Assert -----
            result shouldBe SearchHistoryEntity(previousQueries = emptyList())
        }

        @Test
        fun `returns defaultValue when JSON is malformed`() = runTest {
            // ----- Arrange -----
            val source = Buffer().writeUtf8("not-valid-json")

            // ----- Act -----
            val result = SearchHistorySerializer.readFrom(source)

            // ----- Assert -----
            result shouldBe SearchHistorySerializer.defaultValue
        }

        @Test
        fun `returns defaultValue when input is an empty byte array`() = runTest {
            // ----- Arrange -----
            val source = Buffer()

            // ----- Act -----
            val result = SearchHistorySerializer.readFrom(source)

            // ----- Assert -----
            result shouldBe SearchHistorySerializer.defaultValue
        }

        @Test
        fun `ignores unknown keys when deserializing`() = runTest {
            // ----- Arrange -----
            val json = """{"previousQueries":["one"],"unknownField":"ignored"}"""
            val source = Buffer().writeUtf8(json)

            // ----- Act -----
            val result = SearchHistorySerializer.readFrom(source)

            // ----- Assert -----
            result shouldBe SearchHistoryEntity(previousQueries = listOf("one"))
        }
    }

    @Nested
    inner class WriteTo {
        @Test
        fun `serializes entity with queries to valid JSON`() = runTest {
            // ----- Arrange -----
            val entity = SearchHistoryEntity(previousQueries = listOf("clean code", "refactoring"))
            val sink = Buffer()

            // ----- Act -----
            SearchHistorySerializer.writeTo(
                t = entity,
                sink = sink,
            )

            // ----- Assert -----
            val written = sink.readUtf8()
            val reparsed = SearchHistorySerializer.readFrom(Buffer().writeUtf8(written))
            reparsed shouldBe entity
        }

        @Test
        fun `serializes entity with empty list to valid JSON`() = runTest {
            // ----- Arrange -----
            val entity = SearchHistoryEntity(previousQueries = emptyList())
            val sink = Buffer()

            // ----- Act -----
            SearchHistorySerializer.writeTo(
                t = entity,
                sink = sink,
            )

            // ----- Assert -----
            val written = sink.readUtf8()
            val reparsed = SearchHistorySerializer.readFrom(Buffer().writeUtf8(written))
            reparsed shouldBe entity
        }

        @Test
        fun `round-trips a single query string correctly`() = runTest {
            // ----- Arrange -----
            val entity = SearchHistoryEntity(previousQueries = listOf("single entry"))
            val sink = Buffer()

            // ----- Act -----
            SearchHistorySerializer.writeTo(
                t = entity,
                sink = sink,
            )
            val reparsed = SearchHistorySerializer.readFrom(Buffer().writeUtf8(sink.readUtf8()))

            // ----- Assert -----
            reparsed.previousQueries shouldBe listOf("single entry")
        }
    }
}
