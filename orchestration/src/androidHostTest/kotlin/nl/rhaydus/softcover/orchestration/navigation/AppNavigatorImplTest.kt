package nl.rhaydus.softcover.orchestration.navigation

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.BookDetailScreen
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppNavigatorImplTest {
    private val navigator = AppNavigatorImpl()

    @Nested
    inner class Screen {
        @Test
        fun `BookDetail destination maps to BookDetailScreen with matching id`() {
            // ----- Arrange -----
            val destination = ScreenDestination.BookDetail(id = 42)

            // ----- Act -----
            val result = navigator.screen(destination)

            // ----- Assert -----
            result.shouldBeInstanceOf<BookDetailScreen>().id shouldBe 42
        }
    }
}
