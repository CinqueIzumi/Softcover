package nl.rhaydus.softcover.feature.book_detail.data.datasource

import com.apollographql.apollo.ApolloClient
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.FindTagsByUserAndTaggableQuery
import nl.rhaydus.softcover.SaveTagsMutation
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.network.helper.safeMutation
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.feature.book_detail.data.mapper.toUserTag
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserTagsRemoteDataSourceImplTest {

    private lateinit var apolloClient: ApolloClient
    private lateinit var dataSource: UserTagsRemoteDataSourceImpl

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()
        dataSource = UserTagsRemoteDataSourceImpl(apolloClient = apolloClient)

        mockkStatic("nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt")
        mockkStatic("nl.rhaydus.softcover.feature.book_detail.data.mapper.UserTagMapperKt")
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun stubUserTag(name: String = "Cozy"): UserTag = UserTag(
        name = name,
        category = TagCategory.MOOD,
        count = 3,
        spoiler = false,
    )

    @Nested
    inner class GetUserTags {

        @Test
        fun `queries with userId, type Book, and bookId as Long`() = runTest {
            // ----- Arrange -----
            val userId = 7
            val bookId = 42
            val queryData = mockk<FindTagsByUserAndTaggableQuery.Data>()
            val tagging = mockk<FindTagsByUserAndTaggableQuery.Data.Tagging>()
            val userTag = stubUserTag()

            coEvery {
                apolloClient.safeQuery(
                    query = FindTagsByUserAndTaggableQuery(
                        userId = userId,
                        type = "Book",
                        id = bookId.toLong(),
                    ),
                )
            } returns queryData

            every { queryData.taggings } returns listOf(tagging)

            every { tagging.toUserTag() } returns userTag

            // ----- Act -----
            val result = dataSource.getUserTags(userId = userId, bookId = bookId)

            // ----- Assert -----
            result shouldBe listOf(userTag)
        }

        @Test
        fun `returns empty list when taggings is empty`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<FindTagsByUserAndTaggableQuery.Data>()

            coEvery {
                apolloClient.safeQuery(query = any<FindTagsByUserAndTaggableQuery>())
            } returns queryData

            every { queryData.taggings } returns emptyList()

            // ----- Act -----
            val result = dataSource.getUserTags(userId = 1, bookId = 1)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `maps each tagging to a UserTag via toUserTag`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<FindTagsByUserAndTaggableQuery.Data>()
            val tagging1 = mockk<FindTagsByUserAndTaggableQuery.Data.Tagging>()
            val tagging2 = mockk<FindTagsByUserAndTaggableQuery.Data.Tagging>()
            val userTag1 = stubUserTag(name = "Cozy")
            val userTag2 = stubUserTag(name = "Dark")

            coEvery {
                apolloClient.safeQuery(query = any<FindTagsByUserAndTaggableQuery>())
            } returns queryData

            every { queryData.taggings } returns listOf(tagging1, tagging2)

            every { tagging1.toUserTag() } returns userTag1

            every { tagging2.toUserTag() } returns userTag2

            // ----- Act -----
            val result = dataSource.getUserTags(userId = 1, bookId = 1)

            // ----- Assert -----
            result shouldBe listOf(userTag1, userTag2)
        }
    }

    @Nested
    inner class SaveTags {

        @Test
        fun `mutates with bookId as Long, type Book, and mapped tags`() = runTest {
            // ----- Arrange -----
            val bookId = 55
            val userTag = stubUserTag()
            val mutationData = mockk<SaveTagsMutation.Data>()
            val upsertTags = mockk<SaveTagsMutation.Data.UpsertTags>()
            val mutationTag = mockk<SaveTagsMutation.Data.UpsertTags.Tag>()
            val expectedUserTag = stubUserTag(name = "Cozy")

            coEvery {
                apolloClient.safeMutation(mutation = any<SaveTagsMutation>())
            } returns mutationData

            every { mutationData.upsertTags } returns upsertTags

            every { upsertTags.tags } returns listOf(mutationTag)

            every { mutationTag.toUserTag() } returns expectedUserTag

            // ----- Act -----
            val result = dataSource.saveTags(bookId = bookId, tags = listOf(userTag))

            // ----- Assert -----
            result shouldBe listOf(expectedUserTag)
        }

        @Test
        fun `returns empty list when upsertTags is null`() = runTest {
            // ----- Arrange -----
            val mutationData = mockk<SaveTagsMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<SaveTagsMutation>())
            } returns mutationData

            every { mutationData.upsertTags } returns null

            // ----- Act -----
            val result = dataSource.saveTags(bookId = 1, tags = emptyList())

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `returns empty list when upsertTags tags list is empty`() = runTest {
            // ----- Arrange -----
            val mutationData = mockk<SaveTagsMutation.Data>()
            val upsertTags = mockk<SaveTagsMutation.Data.UpsertTags>()

            coEvery {
                apolloClient.safeMutation(mutation = any<SaveTagsMutation>())
            } returns mutationData

            every { mutationData.upsertTags } returns upsertTags

            every { upsertTags.tags } returns emptyList()

            // ----- Act -----
            val result = dataSource.saveTags(bookId = 1, tags = emptyList())

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `filters null tag entries from the upsert response`() = runTest {
            // ----- Arrange -----
            val mutationData = mockk<SaveTagsMutation.Data>()
            val upsertTags = mockk<SaveTagsMutation.Data.UpsertTags>()
            val mutationTag = mockk<SaveTagsMutation.Data.UpsertTags.Tag>()
            val expectedUserTag = stubUserTag()

            coEvery {
                apolloClient.safeMutation(mutation = any<SaveTagsMutation>())
            } returns mutationData

            every { mutationData.upsertTags } returns upsertTags

            every { upsertTags.tags } returns listOf(null, mutationTag)

            every { mutationTag.toUserTag() } returns expectedUserTag

            // ----- Act -----
            val result = dataSource.saveTags(bookId = 1, tags = listOf(stubUserTag()))

            // ----- Assert -----
            result shouldBe listOf(expectedUserTag)
        }
    }
}
