package nl.rhaydus.softcover.feature.search.data.datastore.serializer

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

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
            val input = ByteArrayInputStream(json.encodeToByteArray())

            // ----- Act -----
            val result = SearchHistorySerializer.readFrom(input)

            // ----- Assert -----
            result shouldBe SearchHistoryEntity(previousQueries = listOf("kotlin", "android", "jetpack"))
        }

        @Test
        fun `deserializes valid JSON with an empty list`() = runTest {
            // ----- Arrange -----
            val json = """{"previousQueries":[]}"""
            val input = ByteArrayInputStream(json.encodeToByteArray())

            // ----- Act -----
            val result = SearchHistorySerializer.readFrom(input)

            // ----- Assert -----
            result shouldBe SearchHistoryEntity(previousQueries = emptyList())
        }

        @Test
        fun `returns defaultValue when JSON is malformed`() = runTest {
            // ----- Arrange -----
            val input = ByteArrayInputStream("not-valid-json".encodeToByteArray())

            // ----- Act -----
            val result = SearchHistorySerializer.readFrom(input)

            // ----- Assert -----
            result shouldBe SearchHistorySerializer.defaultValue
        }

        @Test
        fun `returns defaultValue when input is an empty byte array`() = runTest {
            // ----- Arrange -----
            val input = ByteArrayInputStream(ByteArray(0))

            // ----- Act -----
            val result = SearchHistorySerializer.readFrom(input)

            // ----- Assert -----
            result shouldBe SearchHistorySerializer.defaultValue
        }

        @Test
        fun `ignores unknown keys when deserializing`() = runTest {
            // ----- Arrange -----
            val json = """{"previousQueries":["one"],"unknownField":"ignored"}"""
            val input = ByteArrayInputStream(json.encodeToByteArray())

            // ----- Act -----
            val result = SearchHistorySerializer.readFrom(input)

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
            val output = ByteArrayOutputStream()

            // ----- Act -----
            SearchHistorySerializer.writeTo(t = entity, output = output)

            // ----- Assert -----
            val written = output.toByteArray().decodeToString()
            val reparsed = SearchHistorySerializer.readFrom(ByteArrayInputStream(written.encodeToByteArray()))
            reparsed shouldBe entity
        }

        @Test
        fun `serializes entity with empty list to valid JSON`() = runTest {
            // ----- Arrange -----
            val entity = SearchHistoryEntity(previousQueries = emptyList())
            val output = ByteArrayOutputStream()

            // ----- Act -----
            SearchHistorySerializer.writeTo(t = entity, output = output)

            // ----- Assert -----
            val written = output.toByteArray().decodeToString()
            val reparsed = SearchHistorySerializer.readFrom(ByteArrayInputStream(written.encodeToByteArray()))
            reparsed shouldBe entity
        }

        @Test
        fun `round-trips a single query string correctly`() = runTest {
            // ----- Arrange -----
            val entity = SearchHistoryEntity(previousQueries = listOf("single entry"))
            val output = ByteArrayOutputStream()

            // ----- Act -----
            SearchHistorySerializer.writeTo(t = entity, output = output)
            val reparsed = SearchHistorySerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            // ----- Assert -----
            reparsed.previousQueries shouldBe listOf("single entry")
        }
    }
}
