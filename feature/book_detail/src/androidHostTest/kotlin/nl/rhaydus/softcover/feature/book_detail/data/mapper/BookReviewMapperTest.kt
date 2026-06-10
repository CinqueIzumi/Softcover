package nl.rhaydus.softcover.feature.book_detail.data.mapper

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.database.mapper.reviewDocumentFromSlate
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
                every { img.url } returns avatarUrl
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
        fun `returns non-null BookReview when review_slate has content`() {
            // ----- Arrange -----
            stubFragment(reviewSlate = slateWithText("A fine read."))
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
                hasSpoilers = false,
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
        fun `maps id from fragment`() {
            // ----- Arrange -----
            stubFragment(
                id = 99,
                reviewSlate = slateWithText("Excellent!"),
            )
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.id shouldBe 99
        }

        @Test
        fun `maps review text from review_slate into reviewDocument plainText`() {
            // ----- Arrange -----
            stubFragment(reviewSlate = slateWithText("Loved every page."))
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewDocument shouldBe reviewDocumentFromSlate(slateWithText("Loved every page."))
        }

        @Test
        fun `maps hasSpoilers from review_has_spoilers field`() {
            // ----- Arrange -----
            stubFragment(
                reviewSlate = slateWithText("Watch out!"),
                hasSpoilers = true,
            )
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.hasSpoilers shouldBe true
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
            result!!.rating shouldBe null
        }

        @Test
        fun `maps rating from fragment when present`() {
            // ----- Arrange -----
            stubFragment(
                reviewSlate = slateWithText("Five stars!"),
                rating = 5.0,
            )
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.rating shouldBe 5.0
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
            result!!.reviewedAt shouldBe null
        }

        @Test
        fun `maps likesCount from fragment`() {
            // ----- Arrange -----
            stubFragment(
                reviewSlate = slateWithText("Many liked this."),
                likesCount = 42,
            )
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.likesCount shouldBe 42
        }

        @Test
        fun `maps reviewer id from user`() {
            // ----- Arrange -----
            stubFragment(reviewSlate = slateWithText("Good one."))
            stubUser(id = 77)

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewer.id shouldBe 77
        }

        @Test
        fun `maps reviewer username by calling toString on user username`() {
            // ----- Arrange -----
            stubFragment(reviewSlate = slateWithText("Hello world."))
            stubUser(username = "bob123")

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewer.username shouldBe "bob123"
        }

        @Test
        fun `maps reviewer name as null when user name is null`() {
            // ----- Arrange -----
            stubFragment(reviewSlate = slateWithText("Anonymous reviewer."))
            stubUser(name = null)

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewer.name shouldBe null
        }

        @Test
        fun `maps reviewer avatarUrl as null when user image is null`() {
            // ----- Arrange -----
            stubFragment(reviewSlate = slateWithText("No avatar."))
            stubUser(avatarUrl = null)

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewer.avatarUrl shouldBe null
        }

        @Test
        fun `maps reviewer avatarUrl from user image url when present`() {
            // ----- Arrange -----
            stubFragment(reviewSlate = slateWithText("Has avatar."))
            stubUser(avatarUrl = "https://cdn.example.com/pic.jpg")

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewer.avatarUrl shouldBe "https://cdn.example.com/pic.jpg"
        }
    }
}
