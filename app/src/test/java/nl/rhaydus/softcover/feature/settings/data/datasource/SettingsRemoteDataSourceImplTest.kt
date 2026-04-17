package nl.rhaydus.softcover.feature.settings.data.datasource

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
import nl.rhaydus.softcover.GetUserProfileDataQuery
import nl.rhaydus.softcover.core.data.network.helper.safeQuery
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
        dataSource = SettingsRemoteDataSourceImpl(apolloClient = apolloClient)

        mockkStatic("nl.rhaydus.softcover.core.data.network.helper.ApolloExtensionsKt")
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
            val meEntry = GetUserIdQuery.Data.Me(__typename = "users", id = expectedId)

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

    @Nested
    inner class GetUserProfileData {

        @Test
        fun `returns UserProfileData mapped from first me entry when query succeeds`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()
            val image = GetUserProfileDataQuery.Data.Me.Image(__typename = "images", url = "https://example.com/img.png")
            val userBook = GetUserProfileDataQuery.Data.Me.User_book(__typename = "user_books", read_count = 1, id = 1)
            val meEntry = GetUserProfileDataQuery.Data.Me(
                __typename = "users",
                bio = "A lover of books",
                image = image,
                name = "Jane Doe",
                user_books = listOf(userBook),
            )

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            // ----- Act -----
            val result = dataSource.getUserProfileData()

            // ----- Assert -----
            result.name shouldBe "Jane Doe"
            result.bio shouldBe "A lover of books"
            result.profileImageUrl shouldBe "https://example.com/img.png"
            result.booksRead shouldBe 1
        }

        @Test
        fun `returns empty strings for null name and bio fields`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()
            val meEntry = GetUserProfileDataQuery.Data.Me(
                __typename = "users",
                bio = null,
                image = null,
                name = null,
                user_books = emptyList(),
            )

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            // ----- Act -----
            val result = dataSource.getUserProfileData()

            // ----- Assert -----
            result.name shouldBe ""
            result.bio shouldBe ""
            result.profileImageUrl shouldBe ""
            result.booksRead shouldBe 0
        }

        @Test
        fun `returns empty image url when image is null`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()
            val meEntry = GetUserProfileDataQuery.Data.Me(
                __typename = "users",
                bio = "bio",
                image = null,
                name = "Name",
                user_books = emptyList(),
            )

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            // ----- Act -----
            val result = dataSource.getUserProfileData()

            // ----- Assert -----
            result.profileImageUrl shouldBe ""
        }

        @Test
        fun `returns empty image url when image url is null`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()
            val image = GetUserProfileDataQuery.Data.Me.Image(__typename = "images", url = null)
            val meEntry = GetUserProfileDataQuery.Data.Me(
                __typename = "users",
                bio = "bio",
                image = image,
                name = "Name",
                user_books = emptyList(),
            )

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            // ----- Act -----
            val result = dataSource.getUserProfileData()

            // ----- Assert -----
            result.profileImageUrl shouldBe ""
        }

        @Test
        fun `counts all user_books entries as booksRead`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()
            val userBooks = listOf(
                GetUserProfileDataQuery.Data.Me.User_book(__typename = "user_books", read_count = 1, id = 1),
                GetUserProfileDataQuery.Data.Me.User_book(__typename = "user_books", read_count = 2, id = 2),
                GetUserProfileDataQuery.Data.Me.User_book(__typename = "user_books", read_count = 3, id = 3),
            )
            val meEntry = GetUserProfileDataQuery.Data.Me(
                __typename = "users",
                bio = null,
                image = null,
                name = null,
                user_books = userBooks,
            )

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            // ----- Act -----
            val result = dataSource.getUserProfileData()

            // ----- Assert -----
            result.booksRead shouldBe 3
        }

        @Test
        fun `throws when me list is empty`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserProfileDataQuery.Data>()

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } returns queryData

            every {
                queryData.me
            } returns emptyList()

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.getUserProfileData()
            }
        }

        @Test
        fun `throws when safeQuery throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network error")

            coEvery {
                apolloClient.safeQuery(query = any<GetUserProfileDataQuery>())
            } throws expectedError

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                dataSource.getUserProfileData()
            }
        }
    }
}
