package nl.rhaydus.softcover.feature.explore.data.datastore.serializer

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import okio.Buffer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SearchHistorySerializerTest {
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
        fun `returns defaultValue when JSON is malformed`() = runTest {
            // ----- Arrange -----
            val source = Buffer().writeUtf8("not-valid-json")

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
    }
}
