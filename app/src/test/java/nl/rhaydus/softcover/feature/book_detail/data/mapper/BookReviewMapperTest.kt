package nl.rhaydus.softcover.feature.book_detail.data.mapper

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
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

    private fun stubFragment(
        id: Int = 10,
        review: String? = "Great book!",
        hasSpoilers: Boolean = false,
        rating: Double? = 4.5,
        reviewedAt: String? = "2024-01-01",
        likesCount: Int = 3,
    ) {
        every {
            fragment.id
        } returns id

        every {
            fragment.review
        } returns review

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
        fun `returns null when review field is null`() {
            // ----- Arrange -----
            stubFragment(review = null)
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns non-null BookReview when review field is present`() {
            // ----- Arrange -----
            stubFragment(review = "A fine read.")
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result shouldBe BookReview(
                id = 10,
                review = "A fine read.",
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
            stubFragment(id = 99, review = "Excellent!")
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.id shouldBe 99
        }

        @Test
        fun `maps review text from fragment`() {
            // ----- Arrange -----
            stubFragment(review = "Loved every page.")
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.review shouldBe "Loved every page."
        }

        @Test
        fun `maps hasSpoilers from review_has_spoilers field`() {
            // ----- Arrange -----
            stubFragment(review = "Watch out!", hasSpoilers = true)
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.hasSpoilers shouldBe true
        }

        @Test
        fun `maps rating as null when fragment rating is null`() {
            // ----- Arrange -----
            stubFragment(review = "No stars given.", rating = null)
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.rating shouldBe null
        }

        @Test
        fun `maps rating from fragment when present`() {
            // ----- Arrange -----
            stubFragment(review = "Five stars!", rating = 5.0)
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.rating shouldBe 5.0
        }

        @Test
        fun `maps reviewedAt as null when fragment reviewed_at is null`() {
            // ----- Arrange -----
            stubFragment(review = "Timeless.", reviewedAt = null)
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewedAt shouldBe null
        }

        @Test
        fun `maps likesCount from fragment`() {
            // ----- Arrange -----
            stubFragment(review = "Many liked this.", likesCount = 42)
            stubUser()

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.likesCount shouldBe 42
        }

        @Test
        fun `maps reviewer id from user`() {
            // ----- Arrange -----
            stubFragment(review = "Good one.")
            stubUser(id = 77)

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewer.id shouldBe 77
        }

        @Test
        fun `maps reviewer username by calling toString on user username`() {
            // ----- Arrange -----
            stubFragment(review = "Hello world.")
            stubUser(username = "bob123")

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewer.username shouldBe "bob123"
        }

        @Test
        fun `maps reviewer name as null when user name is null`() {
            // ----- Arrange -----
            stubFragment(review = "Anonymous reviewer.")
            stubUser(name = null)

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewer.name shouldBe null
        }

        @Test
        fun `maps reviewer avatarUrl as null when user image is null`() {
            // ----- Arrange -----
            stubFragment(review = "No avatar.")
            stubUser(avatarUrl = null)

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewer.avatarUrl shouldBe null
        }

        @Test
        fun `maps reviewer avatarUrl from user image url when present`() {
            // ----- Arrange -----
            stubFragment(review = "Has avatar.")
            stubUser(avatarUrl = "https://cdn.example.com/pic.jpg")

            // ----- Act -----
            val result = fragment.toBookReview()

            // ----- Assert -----
            result!!.reviewer.avatarUrl shouldBe "https://cdn.example.com/pic.jpg"
        }
    }
}
