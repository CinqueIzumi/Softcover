package nl.rhaydus.softcover.feature.book_detail.data.datasource

import com.apollographql.apollo.ApolloClient
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.FindUserTagVocabularyQuery
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.network.helper.safeQuery
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserTagVocabularyRemoteDataSourceImplTest {
    companion object {
        private const val APOLLO_EXTENSIONS = "nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt"

        @JvmStatic
        @BeforeAll
        fun mockStatics() {
            mockkStatic(APOLLO_EXTENSIONS)
        }

        @JvmStatic
        @AfterAll
        fun unmockStatics() {
            unmockkAll()
        }
    }

    private lateinit var apolloClient: ApolloClient
    private lateinit var dataSource: UserTagVocabularyRemoteDataSourceImpl

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()
        dataSource = UserTagVocabularyRemoteDataSourceImpl(apolloClient = apolloClient)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    inner class FetchUserVocabulary {
        private fun stubVocabularyTag(
            tag: String,
            category: String?,
            aggregateCount: Int?,
        ): FindUserTagVocabularyQuery.Data.Tag =
            FindUserTagVocabularyQuery.Data.Tag(
                __typename = "tags",
                id = 1L,
                tag = tag,
                tag_category = FindUserTagVocabularyQuery.Data.Tag.Tag_category(
                    __typename = "tag_categories",
                    category = category,
                ),
                taggings_aggregate = FindUserTagVocabularyQuery.Data.Tag.Taggings_aggregate(
                    __typename = "taggings_aggregate",
                    aggregate = aggregateCount?.let {
                        FindUserTagVocabularyQuery.Data.Tag.Taggings_aggregate.Aggregate(
                            __typename = "taggings_aggregate_fields",
                            count = it,
                        )
                    },
                ),
            )

        @Test
        fun `fetches a single short page and maps counts from each tag's aggregate`() = runTest {
            // ----- Arrange -----
            val userId = 3
            val queryData = mockk<FindUserTagVocabularyQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = FindUserTagVocabularyQuery(
                        userId = userId,
                        limit = 100,
                        offset = 0,
                    ),
                )
            } returns queryData

            every {
                queryData.tags
            } returns listOf(
                stubVocabularyTag(
                    tag = "Cozy",
                    category = "Mood",
                    aggregateCount = 9,
                ),
                stubVocabularyTag(
                    tag = "Romance",
                    category = "Genre",
                    aggregateCount = 4,
                ),
            )

            // ----- Act -----
            val result = dataSource.fetchUserVocabulary(userId = userId)

            // ----- Assert -----
            coVerify(exactly = 1) {
                apolloClient.safeQuery(query = any<FindUserTagVocabularyQuery>())
            }

            result shouldBe listOf(
                UserTag(
                    name = "Cozy",
                    category = TagCategory.MOOD,
                    count = 9,
                    spoiler = false,
                ),
                UserTag(
                    name = "Romance",
                    category = TagCategory.GENRE,
                    count = 4,
                    spoiler = false,
                ),
            )
        }

        @Test
        fun `pages through multiple requests and sums aggregate counts for a tag repeated across pages`() = runTest {
            // ----- Arrange -----
            val userId = 3
            val page1Data = mockk<FindUserTagVocabularyQuery.Data>()
            val page2Data = mockk<FindUserTagVocabularyQuery.Data>()

            val page1Tags = buildTagsPage(
                size = 100,
                namePrefix = "page1",
                sharedAggregateCount = 2,
            )
            val page2Tags = buildTagsPage(
                size = 40,
                namePrefix = "page2",
                sharedAggregateCount = 3,
            )

            coEvery {
                apolloClient.safeQuery(
                    query = FindUserTagVocabularyQuery(
                        userId = userId,
                        limit = 100,
                        offset = 0,
                    ),
                )
            } returns page1Data

            coEvery {
                apolloClient.safeQuery(
                    query = FindUserTagVocabularyQuery(
                        userId = userId,
                        limit = 100,
                        offset = 100,
                    ),
                )
            } returns page2Data

            every {
                page1Data.tags
            } returns page1Tags

            every {
                page2Data.tags
            } returns page2Tags

            // ----- Act -----
            val result = dataSource.fetchUserVocabulary(userId = userId)

            // ----- Assert -----
            coVerify(exactly = 2) {
                apolloClient.safeQuery(query = any<FindUserTagVocabularyQuery>())
            }

            coVerify {
                apolloClient.safeQuery(
                    query = FindUserTagVocabularyQuery(
                        userId = userId,
                        limit = 100,
                        offset = 100,
                    ),
                )
            }

            val totalTags = page1Tags.size + page2Tags.size
            totalTags shouldBe 140

            val sharedUserTag = result.single { it.category == TagCategory.MOOD && it.name == "Cozy" }
            sharedUserTag.count shouldBe 5

            result.size shouldBe 139
        }

        @Test
        fun `returns empty list when the first page is empty`() = runTest {
            // ----- Arrange -----
            val userId = 3
            val queryData = mockk<FindUserTagVocabularyQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = FindUserTagVocabularyQuery(
                        userId = userId,
                        limit = 100,
                        offset = 0,
                    ),
                )
            } returns queryData

            every {
                queryData.tags
            } returns emptyList()

            // ----- Act -----
            val result = dataSource.fetchUserVocabulary(userId = userId)

            // ----- Assert -----
            coVerify(exactly = 1) {
                apolloClient.safeQuery(query = any<FindUserTagVocabularyQuery>())
            }

            result shouldBe emptyList()
        }

        private fun buildTagsPage(
            size: Int,
            namePrefix: String,
            sharedAggregateCount: Int,
        ): List<FindUserTagVocabularyQuery.Data.Tag> {
            val sharedTag = stubVocabularyTag(
                tag = "Cozy",
                category = "Mood",
                aggregateCount = sharedAggregateCount,
            )

            val distinctTags = (0 until size - 1).map { index ->
                stubVocabularyTag(
                    tag = "$namePrefix-$index",
                    category = "Genre",
                    aggregateCount = 1,
                )
            }

            return listOf(sharedTag) + distinctTags
        }
    }
}
