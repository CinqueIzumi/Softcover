package nl.rhaydus.softcover.feature.book_detail.data.mapper

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReviewer
import nl.rhaydus.softcover.fragment.BookReviewFragment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookReviewMapperTest {
    private lateinit var fragment: BookReviewFragment
    private lateinit var user: BookReviewFragment.User

    @BeforeEach
    fun setUp() {
        user = mockk()
        fragment = mockk()

        every {
            fragment.user
        } returns user
    }

    private fun stubUser(
        id: Int = 1,
        username: Any? = "alice",
        name: String? = "Alice",
        avatarUrl: String? = "https://example.com/avatar.png",
    ) {
        every {
            user.id
        } returns id

        every {
            user.username
        } returns username

        every {
            user.name
        } returns name

        val image: BookReviewFragment.User.Image? = if (avatarUrl != null) {
            mockk<BookReviewFragment.User.Image>().also { img ->
                every {
                    img.url
                } returns avatarUrl
            }
        } else {
            null
        }

        every {
            user.image
        } returns image
    }

    private fun slateWithText(text: String): List<Map<String, Any?>> = listOf(
        mapOf(
            "data" to emptyMap<String, Any?>(),
            "type" to "paragraph",
            "object" to "block",
            "children" to listOf(mapOf("object" to "text", "text" to text)),
        ),
    )

    private fun stubFragment(
        id: Int = 10,
        reviewSlate: List<Map<String, Any?>> = slateWithText("Great book!"),
        hasSpoilers: Boolean = false,
        rating: Double? = 4.5,
        reviewedAt: String? = "2024-01-01",
        likesCount: Int = 3,
    ) {
        every {
            fragment.id
        } returns id

        every {
            fragment.review_slate
        } returns reviewSlate

        every {
            fragment.review_has_spoilers
        } returns hasSpoilers

        every {
            fragment.rating
        } returns rating

        every {
            fragment.reviewed_at
        } returns reviewedAt

        every {
            fragment.likes_count
        } returns likesCount
    }

    @Nested
    inner class ToBookReview {
        @Test
        fun `returns null when review_slate is empty`() {
            // ----- Arrange -----
            stubFragment(reviewSlate = emptyList())
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `maps every fragment and user field when review_slate has content`() {
            // ----- Arrange -----
            stubFragment(
                reviewSlate = slateWithText("A fine read."),
                hasSpoilers = true,
            )
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result shouldBe BookReview(
                id = 10,
                reviewDocument = ReviewDocument(
                    paragraphs = listOf(
                        ReviewParagraph(runs = listOf(ReviewRun(text = "A fine read."))),
                    ),
                ),
                hasSpoilers = true,
                rating = 4.5,
                reviewedAt = "2024-01-01",
                likesCount = 3,
                reviewer = BookReviewer(
                    id = 1,
                    username = "alice",
                    name = "Alice",
                    avatarUrl = "https://example.com/avatar.png",
                ),
            )
        }

        @Test
        fun `maps rating as null when fragment rating is null`() {
            // ----- Arrange -----
            stubFragment(
                reviewSlate = slateWithText("No stars given."),
                rating = null,
            )
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result.shouldNotBeNull().rating shouldBe null
        }

        @Test
        fun `maps reviewedAt as null when fragment reviewed_at is null`() {
            // ----- Arrange -----
            stubFragment(
                reviewSlate = slateWithText("Timeless."),
                reviewedAt = null,
            )
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result.shouldNotBeNull().reviewedAt shouldBe null
        }

        @Test
        fun `maps reviewer name as null when user name is null`() {
            // ----- Arrange -----
            stubFragment(reviewSlate = slateWithText("Anonymous reviewer."))
            stubUser(name = null)

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result.shouldNotBeNull().reviewer.name shouldBe null
        }

        @Test
        fun `maps reviewer avatarUrl as null when user image is null`() {
            // ----- Arrange -----
            stubFragment(reviewSlate = slateWithText("No avatar."))
            stubUser(avatarUrl = null)

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result.shouldNotBeNull().reviewer.avatarUrl shouldBe null
        }
    }
}
