package nl.rhaydus.softcover.feature.lists.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PrivacyToggleModelTest {
    @Nested
    inner class PrivacyToggleFor {
        @Test
        fun `offers public then private`() {
            // ----- Arrange & Act -----
            val result = privacyToggleFor(selected = PrivacySetting.PUBLIC)

            // ----- Assert -----
            result.options.map { it.key } shouldBe listOf(
                PrivacySetting.PUBLIC,
                PrivacySetting.PRIVATE,
            )
        }

        @Test
        fun `selects public for PUBLIC`() {
            // ----- Arrange & Act -----
            val result = privacyToggleFor(selected = PrivacySetting.PUBLIC)

            // ----- Assert -----
            result.selected shouldBe PrivacySetting.PUBLIC
        }

        @Test
        fun `selects private for PRIVATE`() {
            // ----- Arrange & Act -----
            val result = privacyToggleFor(selected = PrivacySetting.PRIVATE)

            // ----- Assert -----
            result.selected shouldBe PrivacySetting.PRIVATE
        }

        @Test
        fun `reads FOLLOWERS as public so a word is still highlighted`() {
            // ----- Arrange & Act -----
            val result = privacyToggleFor(selected = PrivacySetting.FOLLOWERS)

            // ----- Assert -----
            result.selected shouldBe PrivacySetting.PUBLIC
        }

        @Test
        fun `enables every option`() {
            // ----- Arrange & Act -----
            val result = privacyToggleFor(selected = PrivacySetting.PUBLIC)

            // ----- Assert -----
            result.options.all { it.enabled } shouldBe true
        }
    }
}
