package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TagNamingFieldBuilderTest {
    private val addPill = PillButtonUiModel(
        key = "add-test",
        content = PillButtonContent.Label(text = "Add"),
    )

    @Nested
    inner class TagNamingFieldFor {
        @Test
        fun `shows the Add pill for a non-blank draft`() {
            // ----- Arrange -----
            val input = "cozy"

            // ----- Act -----
            val field = tagNamingFieldFor(
                input = input,
                category = TagCategory.MOOD,
                addPill = addPill,
            )

            // ----- Assert -----
            val variant = field.variant.shouldBeInstanceOf<TextFieldVariant.Pill>()
            variant.trailing shouldBe addPill
        }

        @Test
        fun `hides the Add pill for a whitespace-only draft`() {
            // ----- Arrange -----
            val input = "   "

            // ----- Act -----
            val field = tagNamingFieldFor(
                input = input,
                category = TagCategory.GENRE,
                addPill = addPill,
            )

            // ----- Assert -----
            val variant = field.variant.shouldBeInstanceOf<TextFieldVariant.Pill>()
            variant.trailing shouldBe null
        }

        @Test
        fun `gives each category its own placeholder`() {
            // ----- Arrange -----
            val categories = listOf(
                TagCategory.GENRE,
                TagCategory.MOOD,
                TagCategory.CONTENT_WARNING,
            )

            // ----- Act -----
            val placeholders = categories.map {
                tagNamingFieldFor(
                    input = "",
                    category = it,
                    addPill = addPill,
                ).placeholder
            }

            // ----- Assert -----
            placeholders.toSet().size shouldBe categories.size
        }
    }

    @Nested
    inner class WithTagDraft {
        @Test
        fun `stores the draft and mirrors it onto the naming field text`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState()

            // ----- Act -----
            val result = state.withTagDraft(input = "slow burn")

            // ----- Assert -----
            result.tagEditorInput shouldBe "slow burn"
            result.tagNamingField.text shouldBe "slow burn"
        }

        @Test
        fun `keeps the naming field placeholder and variant`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState()

            // ----- Act -----
            val result = state.withTagDraft(input = "slow burn")

            // ----- Assert -----
            result.tagNamingField.placeholder shouldBe state.tagNamingField.placeholder
            result.tagNamingField.variant shouldBe state.tagNamingField.variant
        }
    }
}
