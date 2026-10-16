package nl.rhaydus.softcover.core.personal.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.InsertQuoteMutation
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.database.model.QuoteSyncState
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.network.helper.safeMutation
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteRemoteDataSourceImplTest {
    private lateinit var apolloClient: ApolloClient
    private lateinit var dataSource: QuoteRemoteDataSourceImpl

    companion object {
        private const val APOLLO_EXTENSIONS = "nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt"

        @JvmStatic
        @BeforeAll
        fun mockApolloExtensions() {
            mockkStatic(APOLLO_EXTENSIONS)
        }

        @JvmStatic
        @AfterAll
        fun unmockApolloExtensions() {
            unmockkStatic(APOLLO_EXTENSIONS)
        }
    }

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()
        dataSource = QuoteRemoteDataSourceImpl(
            apolloClient = apolloClient,
            getUserIdUseCase = mockk<GetUserIdUseCase>(),
        )
    }

    private fun entity() = QuoteEntity(
        localId = 3,
        remoteId = null,
        bookId = 11,
        editionId = 22,
        text = "A quote",
        privacySettingId = 3,
        actionAtEpochMillis = 1_709_280_000_000,
        positionType = "pages",
        positionValue = 24.0,
        positionPossible = 400.0,
        positionPercent = 6.0,
        syncState = QuoteSyncState.PENDING_INSERT,
    )

    private fun data(
        id: Int?,
        errors: List<String?>?,
    ) = InsertQuoteMutation.Data(
        insert_reading_journal = InsertQuoteMutation.Data.Insert_reading_journal(
            __typename = "InsertReadingJournalOutput",
            id = id,
            errors = errors,
        ),
    )

    @Nested
    inner class InsertQuote {
        @Test
        fun `sends a quote event with no tags, the entity's fields and the position metadata`() = runTest {
            // ----- Arrange -----
            val captured = slot<InsertQuoteMutation>()

            coEvery {
                apolloClient.safeMutation(mutation = capture(captured))
            } returns data(
                id = 901,
                errors = null,
            )

            // ----- Act -----
            val result = dataSource.insertQuote(entity = entity())

            // ----- Assert -----
            val sent = captured.captured.`object`

            result shouldBe 901L
            sent.event shouldBe "quote"
            sent.tags shouldBe emptyList()
            sent.book_id shouldBe 11
            sent.edition_id shouldBe Optional.Present(22)
            sent.entry shouldBe Optional.Present("A quote")
            sent.privacy_setting_id shouldBe 3
            sent.action_at shouldBe Optional.Present("2024-03-01T08:00:00Z")
            sent.metadata shouldBe Optional.Present(
                mapOf(
                    "position" to mapOf(
                        "type" to "pages",
                        "value" to 24,
                        "possible" to 400,
                        "percent" to 6.0,
                    ),
                ),
            )
        }

        @Test
        fun `throws when the server returns errors`() = runTest {
            // ----- Arrange -----
            coEvery {
                apolloClient.safeMutation(mutation = any<InsertQuoteMutation>())
            } returns data(
                id = null,
                errors = listOf("Not allowed"),
            )

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> { dataSource.insertQuote(entity = entity()) }
        }

        @Test
        fun `throws when the response carries no id`() = runTest {
            // ----- Arrange -----
            coEvery {
                apolloClient.safeMutation(mutation = any<InsertQuoteMutation>())
            } returns data(
                id = null,
                errors = null,
            )

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> { dataSource.insertQuote(entity = entity()) }
        }

        @Test
        fun `throws when the response is missing`() = runTest {
            // ----- Arrange -----
            coEvery {
                apolloClient.safeMutation(mutation = any<InsertQuoteMutation>())
            } returns InsertQuoteMutation.Data(insert_reading_journal = null)

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> { dataSource.insertQuote(entity = entity()) }
        }
    }
}
