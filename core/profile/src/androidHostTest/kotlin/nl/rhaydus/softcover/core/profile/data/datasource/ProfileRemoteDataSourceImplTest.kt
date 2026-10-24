package nl.rhaydus.softcover.core.profile.data.datasource

import com.apollographql.apollo.ApolloClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import nl.rhaydus.softcover.GetReadingActivityDaysQuery
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
            timeZone = TimeZone.UTC,
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
        fun `maps a null image to an empty profileImageUrl`() = runTest {
            // ----- Arrange -----
            mockProfileQuery(me(image = null))

            // ----- Act -----
            val result = dataSource.getUserProfileSnapshot()

            // ----- Assert -----
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

    @Nested
    inner class StreamReadingDaysDescending {
        private fun readingActivityPage(vararg actionAts: String): GetReadingActivityDaysQuery.Data =
            GetReadingActivityDaysQuery.Data(
                reading_journals = actionAts.map {
                    GetReadingActivityDaysQuery.Data.Reading_journal(
                        __typename = "reading_journals",
                        action_at = it,
                    )
                },
            )

        @Test
        fun `emits parsed dates across pages until an empty page ends the stream`() = runTest {
            // ----- Arrange -----
            val page1 = readingActivityPage(
                "2026-05-04",
                "2026-05-03",
            )
            val page2 = readingActivityPage(
                "2026-05-02",
                "2026-05-01",
            )
            val emptyPage = readingActivityPage()

            coEvery {
                apolloClient.safeQuery(query = any<GetReadingActivityDaysQuery>())
            } returnsMany listOf(page1, page2, emptyPage)

            // ----- Act -----
            val result = dataSource.streamReadingDaysDescending(userId = 42).toList()

            // ----- Assert -----
            result shouldBe listOf(
                LocalDate(
                    2026,
                    5,
                    4,
                ),
                LocalDate(
                    2026,
                    5,
                    3,
                ),
                LocalDate(
                    2026,
                    5,
                    2,
                ),
                LocalDate(
                    2026,
                    5,
                    1,
                ),
            )
            coVerify(exactly = 3) {
                apolloClient.safeQuery(query = any<GetReadingActivityDaysQuery>())
            }
        }

        @Test
        fun `advances offset by raw row count including unparseable rows`() = runTest {
            // ----- Arrange -----
            val page1 = readingActivityPage(
                "2026-05-04",
                "not-a-date",
                "2026-05-03",
            )
            val page2 = readingActivityPage()

            coEvery {
                apolloClient.safeQuery(query = any<GetReadingActivityDaysQuery>())
            } returnsMany listOf(page1, page2)

            // ----- Act -----
            val result = dataSource.streamReadingDaysDescending(userId = 42).toList()

            // ----- Assert -----
            result shouldBe listOf(
                LocalDate(
                    2026,
                    5,
                    4,
                ),
                LocalDate(
                    2026,
                    5,
                    3,
                ),
            )
            coVerify {
                apolloClient.safeQuery(
                    query = GetReadingActivityDaysQuery(
                        userId = 42,
                        limit = 100,
                        offset = 3,
                    ),
                )
            }
        }

        @Test
        fun `buckets timestamps by local timezone and passes bare dates through unchanged`() = runTest {
            // ----- Arrange -----
            val utcMinus5 = UtcOffset(hours = -5).asTimeZone()
            val localDataSource = ProfileRemoteDataSourceImpl(
                apolloClient = apolloClient,
                timeZone = utcMinus5,
            )

            val page = readingActivityPage(
                "2026-05-17T23:15:05+00:00",
                "2026-05-19T04:00:00+00:00",
                "2026-05-02",
            )
            val emptyPage = readingActivityPage()

            coEvery {
                apolloClient.safeQuery(query = any<GetReadingActivityDaysQuery>())
            } returnsMany listOf(page, emptyPage)

            // ----- Act -----
            val result = localDataSource.streamReadingDaysDescending(userId = 42).toList()

            // ----- Assert -----
            result shouldBe listOf(
                LocalDate(
                    2026,
                    5,
                    17,
                ),
                LocalDate(
                    2026,
                    5,
                    18,
                ),
                LocalDate(
                    2026,
                    5,
                    2,
                ),
            )
        }
    }
}
