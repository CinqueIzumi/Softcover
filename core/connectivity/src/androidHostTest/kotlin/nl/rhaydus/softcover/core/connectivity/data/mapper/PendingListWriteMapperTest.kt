package nl.rhaydus.softcover.core.connectivity.data.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.model.PendingListWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PendingListWriteMapperTest {
    private fun write(
        kind: PendingListWriteKind = PendingListWriteKind.ADD_LIST_BOOK,
        orderedListBookIds: List<Int>? = null,
        privacy: PrivacySetting? = null,
    ) = PendingListWrite(
        kind = kind,
        listId = 1,
        listName = "Favourites",
        bookId = 2,
        editionId = 3,
        listBookId = 4,
        startPosition = 5,
        orderedListBookIds = orderedListBookIds,
        enqueuedAt = "2026-01-01T00:00:00Z",
        privacy = privacy,
    )

    private fun entity(
        kind: String = "ADD_LIST_BOOK",
        orderedListBookIdsCsv: String? = null,
        privacySettingId: Int? = null,
    ) = PendingListWriteEntity(
        kind = kind,
        listId = 1,
        listName = "Favourites",
        bookId = 2,
        editionId = 3,
        listBookId = 4,
        startPosition = 5,
        orderedListBookIdsCsv = orderedListBookIdsCsv,
        enqueuedAt = "2026-01-01T00:00:00Z",
        privacySettingId = privacySettingId,
    )

    @Nested
    inner class ToEntity {
        @Test
        fun `maps every field and names the kind by its enum name`() {
            // ----- Arrange -----
            val input = write(kind = PendingListWriteKind.REMOVE_LIST_BOOK)

            // ----- Act -----
            val result = input.toEntity()

            // ----- Assert -----
            result shouldBe entity(kind = "REMOVE_LIST_BOOK")
        }

        @Test
        fun `joins the ordered ids into a comma separated string`() {
            // ----- Arrange -----
            val input = write(orderedListBookIds = listOf(3, 1, 2))

            // ----- Act -----
            val result = input.toEntity()

            // ----- Assert -----
            result.orderedListBookIdsCsv shouldBe "3,1,2"
        }

        @Test
        fun `keeps a null ordered id list null`() {
            // ----- Arrange -----
            val input = write(orderedListBookIds = null)

            // ----- Act -----
            val result = input.toEntity()

            // ----- Assert -----
            result.orderedListBookIdsCsv.shouldBeNull()
        }

        @Test
        fun `stores the privacy as its code`() {
            // ----- Arrange -----
            val input = write(privacy = PrivacySetting.PRIVATE)

            // ----- Act -----
            val result = input.toEntity()

            // ----- Assert -----
            result.privacySettingId shouldBe PrivacySetting.PRIVATE.code
        }

        @Test
        fun `stores no privacy code when privacy is null`() {
            // ----- Arrange -----
            val input = write(privacy = null)

            // ----- Act -----
            val result = input.toEntity()

            // ----- Assert -----
            result.privacySettingId.shouldBeNull()
        }
    }

    @Nested
    inner class ToPendingListWrite {
        @Test
        fun `maps every field of a valid entity`() {
            // ----- Arrange -----
            val input = entity(
                kind = "CREATE_LIST",
                orderedListBookIdsCsv = "7,8",
                privacySettingId = PrivacySetting.FOLLOWERS.code,
            )

            // ----- Act -----
            val result = input.toPendingListWrite()

            // ----- Assert -----
            result shouldBe write(
                kind = PendingListWriteKind.CREATE_LIST,
                orderedListBookIds = listOf(7, 8),
                privacy = PrivacySetting.FOLLOWERS,
            )
        }

        @Test
        fun `returns null for an unknown kind`() {
            // ----- Arrange -----
            val input = entity(kind = "FROM_THE_FUTURE")

            // ----- Act -----
            val result = input.toPendingListWrite()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null for a kind of the wrong case`() {
            // ----- Arrange -----
            val input = entity(kind = "create_list")

            // ----- Act -----
            val result = input.toPendingListWrite()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `maps a null csv to a null id list`() {
            // ----- Arrange -----
            val input = entity(orderedListBookIdsCsv = null)

            // ----- Act -----
            val result = input.toPendingListWrite()

            // ----- Assert -----
            result.shouldNotBeNull().orderedListBookIds.shouldBeNull()
        }

        @Test
        fun `maps a blank csv to a null id list`() {
            // ----- Arrange -----
            val input = entity(orderedListBookIdsCsv = "   ")

            // ----- Act -----
            val result = input.toPendingListWrite()

            // ----- Assert -----
            result.shouldNotBeNull().orderedListBookIds.shouldBeNull()
        }

        @Test
        fun `parses a single id`() {
            // ----- Arrange -----
            val input = entity(orderedListBookIdsCsv = "42")

            // ----- Act -----
            val result = input.toPendingListWrite()

            // ----- Assert -----
            result.shouldNotBeNull().orderedListBookIds shouldBe listOf(42)
        }

        @Test
        fun `trims whitespace around ids and drops unparseable ones`() {
            // ----- Arrange -----
            val input = entity(orderedListBookIdsCsv = " 1, x ,3,,4 ")

            // ----- Act -----
            val result = input.toPendingListWrite()

            // ----- Assert -----
            result.shouldNotBeNull().orderedListBookIds shouldBe listOf(1, 3, 4)
        }

        @Test
        fun `maps an unknown privacy code to null`() {
            // ----- Arrange -----
            val input = entity(privacySettingId = 99)

            // ----- Act -----
            val result = input.toPendingListWrite()

            // ----- Assert -----
            result.shouldNotBeNull().privacy.shouldBeNull()
        }
    }

    @Nested
    inner class RoundTrip {
        @Test
        fun `returns the original write for every kind and privacy`() {
            // ----- Arrange -----
            val inputs = PendingListWriteKind.entries.flatMap { kind ->
                listOf(null).plus(PrivacySetting.entries).map { privacy ->
                    write(
                        kind = kind,
                        orderedListBookIds = listOf(9, 8, 7),
                        privacy = privacy,
                    )
                }
            }

            // ----- Act -----
            val results = inputs.map { it.toEntity().toPendingListWrite() }

            // ----- Assert -----
            results shouldBe inputs
        }

        @Test
        fun `preserves an empty id list as null`() {
            // ----- Arrange -----
            val input = write(orderedListBookIds = emptyList())

            // ----- Act -----
            val result = input.toEntity().toPendingListWrite()

            // ----- Assert -----
            result.shouldNotBeNull().orderedListBookIds.shouldBeNull()
        }
    }
}
