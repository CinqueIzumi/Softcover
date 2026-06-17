package nl.rhaydus.softcover.core.preferences.data.datasource

import com.apollographql.apollo.ApolloClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.GetUserIdQuery
import nl.rhaydus.softcover.core.network.helper.safeQuery
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SettingsRemoteDataSourceImplTest {
    private lateinit var apolloClient: ApolloClient
    private lateinit var dataSource: SettingsRemoteDataSourceImpl

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()
        dataSource =
            SettingsRemoteDataSourceImpl(
                apolloClient = apolloClient,
            )

        mockkStatic("nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt")
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Nested
    inner class GetUserIdFromBackend {
        @Test
        fun `returns id from first me entry when query succeeds`() = runTest {
            // ----- Arrange -----
            val expectedId = 42
            val queryData = mockk<GetUserIdQuery.Data>()
            val meEntry = GetUserIdQuery.Data.Me(
                __typename = "users",
                id = expectedId,
            )

            coEvery {
                apolloClient.safeQuery(query = any<GetUserIdQuery>())
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            // ----- Act -----
            val result = dataSource.getUserIdFromBackend()

            // ----- Assert -----
            result shouldBe expectedId
        }

        @Test
        fun `throws when me list is empty`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserIdQuery.Data>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserIdQuery>())
            } returns queryData

            every {
                queryData.me
            } returns emptyList()

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.getUserIdFromBackend()
            }
        }

        @Test
        fun `throws when safeQuery throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network error")

            coEvery {
                apolloClient.safeQuery(query = any<GetUserIdQuery>())
            } throws expectedError

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                dataSource.getUserIdFromBackend()
            }
        }
    }
}
