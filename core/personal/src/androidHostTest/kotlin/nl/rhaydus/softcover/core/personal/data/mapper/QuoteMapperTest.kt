package nl.rhaydus.softcover.core.personal.data.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.database.model.QuoteSyncState
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import nl.rhaydus.softcover.fragment.QuoteFieldsFragment
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class QuoteMapperTest {
    private fun entity(
        privacySettingId: Int = 1,
        syncState: QuoteSyncState = QuoteSyncState.SYNCED,
        positionType: String? = null,
        positionValue: Double? = null,
        positionPossible: Double? = null,
        positionPercent: Double? = null,
        editionTitle: String? = null,
        editionCoverUrl: String? = null,
    ) = QuoteEntity(
        localId = 5,
        remoteId = 77,
        bookId = 1,
        editionId = 2,
        text = "A quote",
        privacySettingId = privacySettingId,
        actionAtEpochMillis = 1_700_000_000_000,
        positionType = positionType,
        positionValue = positionValue,
        positionPossible = positionPossible,
        positionPercent = positionPercent,
        syncState = syncState,
        editionTitle = editionTitle,
        editionCoverUrl = editionCoverUrl,
    )

    private class FakeBook(override val title: String?) : QuoteFieldsFragment.Book {
        override val __typename: String = "books"
        override val id: Int = 1
    }

    private class FakeImage(override val url: String?) : QuoteFieldsFragment.Edition.Image {
        override val __typename: String = "images"
    }

    private class FakeEdition(
        override val title: String?,
        override val image: QuoteFieldsFragment.Edition.Image?,
    ) : QuoteFieldsFragment.Edition {
        override val __typename: String = "editions"
        override val id: Int = 3
    }

    private class FakeFragment(
        override val entry: String?,
        override val action_at: String,
        override val metadata: Any,
        override val book_id: Int?,
        override val book: QuoteFieldsFragment.Book?,
        override val edition: QuoteFieldsFragment.Edition?,
    ) : QuoteFieldsFragment {
        override val __typename: String = "reading_journals"
        override val id: Long = 9L
        override val privacy_setting_id: Int = 3
        override val edition_id: Int? = 3
    }

    private fun journal(
        entry: String? = "A quote",
        actionAt: String = "2024-03-01T08:00:00+00:00",
        metadata: Any = emptyMap<String, Any?>(),
        bookId: Int? = 1,
        bookTitle: String? = "Dune",
        edition: QuoteFieldsFragment.Edition? = null,
    ): QuoteFieldsFragment = FakeFragment(
        entry = entry,
        action_at = actionAt,
        metadata = metadata,
        book_id = bookId,
        book = FakeBook(title = bookTitle),
        edition = edition,
    )

    @Nested
    inner class EntityToDomain {
        @Test
        fun `maps a known privacy id to its setting`() {
            // ----- Arrange -----
            val entity = entity(privacySettingId = 3)

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.privacy shouldBe PrivacySetting.PRIVATE
        }

        @Test
        fun `falls back to PUBLIC for an unknown privacy id`() {
            // ----- Arrange -----
            val entity = entity(privacySettingId = 99)

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.privacy shouldBe PrivacySetting.PUBLIC
        }

        @Test
        fun `marks only a pending insert as pending`() {
            // ----- Arrange -----
            val states = QuoteSyncState.entries

            // ----- Act -----
            val pending = states.associateWith { entity(syncState = it).toDomain().isPending }

            // ----- Assert -----
            pending shouldBe mapOf(
                QuoteSyncState.SYNCED to false,
                QuoteSyncState.PENDING_INSERT to true,
                QuoteSyncState.PENDING_DELETE to false,
            )
        }

        @Test
        fun `rebuilds the pages position from the stored columns`() {
            // ----- Arrange -----
            val entity = entity(
                positionType = "pages",
                positionValue = 24.0,
                positionPossible = 400.0,
                positionPercent = 6.0,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.position shouldBe QuotePosition.Pages(
                page = 24,
                totalPages = 400,
                percent = 6.0,
            )
        }

        @Test
        fun `has no position when the stored type is unknown`() {
            // ----- Arrange -----
            val entity = entity(
                positionType = "chapters",
                positionValue = 3.0,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.position.shouldBeNull()
        }

        @Test
        fun `has no position when the stored value is missing`() {
            // ----- Arrange -----
            val entity = entity(positionType = "pages")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.position.shouldBeNull()
        }
    }

    @Nested
    inner class EditionRoundTrip {
        @Test
        fun `carries the edition title and cover url from the entity to the domain quote`() {
            // ----- Arrange -----
            val entity = entity(
                editionTitle = "Dune (Illustrated)",
                editionCoverUrl = "https://example.com/cover.jpg",
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.editionTitle shouldBe "Dune (Illustrated)"
            result.editionCoverUrl shouldBe "https://example.com/cover.jpg"
        }
    }

    @Nested
    inner class NewPendingQuoteEntity {
        @Test
        fun `builds an unsynced row carrying the position columns`() {
            // ----- Arrange -----
            val position = QuotePosition.seconds(
                seconds = 900,
                totalSeconds = 3600,
            )

            // ----- Act -----
            val result = newPendingQuoteEntity(
                bookId = 1,
                editionId = null,
                text = "A quote",
                privacy = PrivacySetting.FOLLOWERS,
                actionAt = Instant.fromEpochMilliseconds(5_000),
                position = position,
                bookTitle = "Dune",
                editionTitle = "Dune (Illustrated)",
                editionCoverUrl = "https://example.com/cover.jpg",
            )

            // ----- Assert -----
            result shouldBe QuoteEntity(
                remoteId = null,
                bookId = 1,
                editionId = null,
                text = "A quote",
                privacySettingId = 2,
                actionAtEpochMillis = 5_000,
                positionType = "seconds",
                positionValue = 900.0,
                positionPossible = 3600.0,
                positionPercent = 25.0,
                syncState = QuoteSyncState.PENDING_INSERT,
                bookTitle = "Dune",
                editionTitle = "Dune (Illustrated)",
                editionCoverUrl = "https://example.com/cover.jpg",
            )
        }

        @Test
        fun `leaves the position columns empty without a position`() {
            // ----- Arrange -----
            val position: QuotePosition? = null

            // ----- Act -----
            val result = newPendingQuoteEntity(
                bookId = 1,
                editionId = null,
                text = "A quote",
                privacy = PrivacySetting.PUBLIC,
                actionAt = Instant.fromEpochMilliseconds(5_000),
                position = position,
                bookTitle = null,
                editionTitle = null,
                editionCoverUrl = null,
            )

            // ----- Assert -----
            result.positionType.shouldBeNull()
            result.positionValue.shouldBeNull()
        }
    }

    @Nested
    inner class JournalToEntity {
        @Test
        fun `maps a journal row to a synced entity`() {
            // ----- Arrange -----
            val row = journal(
                metadata = mapOf(
                    "position" to mapOf(
                        "type" to "pages",
                        "value" to 24,
                        "possible" to 400,
                        "percent" to 6.0,
                    ),
                ),
            )

            // ----- Act -----
            val result = row.toQuoteEntity()

            // ----- Assert -----
            result shouldBe QuoteEntity(
                remoteId = 9,
                bookId = 1,
                editionId = 3,
                text = "A quote",
                privacySettingId = 3,
                actionAtEpochMillis = Instant.parse("2024-03-01T08:00:00Z").toEpochMilliseconds(),
                positionType = "pages",
                positionValue = 24.0,
                positionPossible = 400.0,
                positionPercent = 6.0,
                syncState = QuoteSyncState.SYNCED,
                bookTitle = "Dune",
            )
        }

        @Test
        fun `maps the edition title and image url`() {
            // ----- Arrange -----
            val row = journal(
                edition = FakeEdition(
                    title = "Dune (Illustrated)",
                    image = FakeImage(url = "https://example.com/cover.jpg"),
                ),
            )

            // ----- Act -----
            val result = row.toQuoteEntity()

            // ----- Assert -----
            val entity = result.shouldNotBeNull()
            entity.editionTitle shouldBe "Dune (Illustrated)"
            entity.editionCoverUrl shouldBe "https://example.com/cover.jpg"
        }

        @Test
        fun `leaves the edition fields empty without an edition`() {
            // ----- Arrange -----
            val row = journal(edition = null)

            // ----- Act -----
            val result = row.toQuoteEntity()

            // ----- Assert -----
            val entity = result.shouldNotBeNull()
            entity.editionTitle.shouldBeNull()
            entity.editionCoverUrl.shouldBeNull()
        }

        @Test
        fun `keeps a row without a book title`() {
            // ----- Arrange -----
            val row = journal(bookTitle = null)

            // ----- Act -----
            val result = row.toQuoteEntity()

            // ----- Assert -----
            result.shouldNotBeNull().bookTitle.shouldBeNull()
        }

        @Test
        fun `drops a row without a book id`() {
            // ----- Arrange -----
            val row = journal(bookId = null)

            // ----- Act -----
            val result = row.toQuoteEntity()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `drops a row whose entry is blank`() {
            // ----- Arrange -----
            val row = journal(entry = "   ")

            // ----- Act -----
            val result = row.toQuoteEntity()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `drops a row whose date cannot be parsed`() {
            // ----- Arrange -----
            val row = journal(actionAt = "yesterday")

            // ----- Act -----
            val result = row.toQuoteEntity()

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
