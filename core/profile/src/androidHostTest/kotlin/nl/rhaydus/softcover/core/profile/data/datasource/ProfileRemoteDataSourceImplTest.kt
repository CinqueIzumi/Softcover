package nl.rhaydus.softcover.core.profile.data.datasource

import com.apollographql.apollo.ApolloClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.GetUserProfileDataQuery
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileRemoteDataSourceImplTest {
    private lateinit var apolloClient: ApolloClient
    private lateinit var dataSource: ProfileRemoteDataSourceImpl

    companion object {
        private const val APOLLO_EXTENSIONS = "nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt"

        // Static-mocking the Apollo extensions is the expensive step, so it happens once per class.
        // Every test stubs against its own fresh ApolloClient mock, so no stub leaks between tests.
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
        dataSource = ProfileRemoteDataSourceImpl(
            apolloClient = apolloClient,
        )
    }

    // region me{} builders
    private fun me(
        bio: String? = "A bio",
        image: GetUserProfileDataQuery.Data.Me.Image? = GetUserProfileDataQuery.Data.Me.Image(
            __typename = "images",
            url = "https://example.com/avatar.jpg",
        ),
        name: String? = "John Doe",
        username: Any? = "johndoe",
    ): GetUserProfileDataQuery.Data.Me = GetUserProfileDataQuery.Data.Me(
        __typename = "users",
        bio = bio,
        image = image,
        name = name,
        username = username,
        id = 1,
    )

    private fun mockProfileQuery(vararg entries: GetUserProfileDataQuery.Data.Me) {
        coEvery {
            apolloClient.safeQuery(query = ofType<GetUserProfileDataQuery>())
        } returns GetUserProfileDataQuery.Data(me = entries.toList())
    }
    // endregion
    @Nested
    inner class GetUserProfileSnapshot {
        @Test
        fun `maps profile fields from the me query`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(
                me(
                    bio = "A bio",
                    name = "John Doe",
                    username = "johndoe",
                ),
            )

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result shouldBe UserProfileSnapshot(
                profileImageUrl = "https://example.com/avatar.jpg",
                name = "John Doe",
                username = "johndoe",
                bio = "A bio",
            )
        }

        @Test
        fun `maps null name, bio, username, image and image url to empty fallbacks`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(
                me(
                    bio = null,
                    image = GetUserProfileDataQuery.Data.Me.Image(
                        __typename = "images",
                        url = null,
                    ),
                    name = null,
                    username = null,
                ),
            )

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
            result.name shouldBe ""
            result.username shouldBe ""
            result.bio shouldBe ""
            result.profileImageUrl shouldBe ""
        }

        @Test
        fun `throws when me list is empty`() = runTest {
            // ----- Arrange -----
            mockProfileQuery()

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.getUserProfileSnapshot()
            }
        }

        @Test
        fun `throws when the profile query fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                apolloClient.safeQuery(query = ofType<GetUserProfileDataQuery>())
            } throws RuntimeException("profile query failed")

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                dataSource.getUserProfileSnapshot()
            }
        }
    }
}
