package nl.rhaydus.softcover.feature.lists.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PrivacyToggleModelTest {
    @Nested
    inner class PrivacyToggleFor {
        @Test
        fun `selects private only for PRIVATE`() {
            // ----- Arrange -----
            val privateSetting = PrivacySetting.PRIVATE
            val publicSetting = PrivacySetting.PUBLIC

            // ----- Act -----
            val private = privacyToggleFor(selected = privateSetting)
            val public = privacyToggleFor(selected = publicSetting)

            // ----- Assert -----
            private.selected shouldBe PrivacySetting.PRIVATE
            public.selected shouldBe PrivacySetting.PUBLIC
        }

        @Test
        fun `reads FOLLOWERS as public so a word is still highlighted`() {
            // ----- Arrange -----
            val setting = PrivacySetting.FOLLOWERS

            // ----- Act -----
            val result = privacyToggleFor(selected = setting)

            // ----- Assert -----
            result.selected shouldBe PrivacySetting.PUBLIC
        }
    }
}
