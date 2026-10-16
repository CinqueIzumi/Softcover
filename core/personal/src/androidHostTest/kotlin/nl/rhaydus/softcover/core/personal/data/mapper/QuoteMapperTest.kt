package nl.rhaydus.softcover.core.personal.data.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.GetBookQuotesQuery
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.database.model.QuoteSyncState
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.QuotePosition
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
    )

    private fun journal(
        entry: String? = "A quote",
        actionAt: String = "2024-03-01T08:00:00+00:00",
        metadata: Any = emptyMap<String, Any?>(),
    ) = GetBookQuotesQuery.Data.Reading_journal(
        __typename = "reading_journals",
        id = 9L,
        entry = entry,
        action_at = actionAt,
        edition_id = 3,
        privacy_setting_id = 3,
        metadata = metadata,
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
            val result = row.toQuoteEntity(bookId = 1)

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
            )
        }

        @Test
        fun `drops a row whose entry is blank`() {
            // ----- Arrange -----
            val row = journal(entry = "   ")

            // ----- Act -----
            val result = row.toQuoteEntity(bookId = 1)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `drops a row whose date cannot be parsed`() {
            // ----- Arrange -----
            val row = journal(actionAt = "yesterday")

            // ----- Act -----
            val result = row.toQuoteEntity(bookId = 1)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
