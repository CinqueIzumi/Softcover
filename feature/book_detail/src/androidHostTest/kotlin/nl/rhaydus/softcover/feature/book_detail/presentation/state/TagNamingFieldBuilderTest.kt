package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldImeAction
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.domain.model.TagCategory
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
        fun `carries the draft text through unchanged`() {
            // ----- Arrange -----
            val input = "  slow burn "

            // ----- Act -----
            val field = tagNamingFieldFor(
                input = input,
                category = TagCategory.TAG,
                addPill = addPill,
            )

            // ----- Assert -----
            field.text shouldBe input
        }

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
        fun `hides the Add pill for an empty draft`() {
            // ----- Arrange -----
            val input = ""

            // ----- Act -----
            val field = tagNamingFieldFor(
                input = input,
                category = TagCategory.MOOD,
                addPill = addPill,
            )

            // ----- Assert -----
            val variant = field.variant.shouldBeInstanceOf<TextFieldVariant.Pill>()
            variant.trailing shouldBe null
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
        fun `asks the keyboard for a Done action`() {
            // ----- Arrange -----
            val input = "cozy"

            // ----- Act -----
            val field = tagNamingFieldFor(
                input = input,
                category = TagCategory.MOOD,
                addPill = addPill,
            )

            // ----- Assert -----
            field.imeAction shouldBe TextFieldImeAction.Done
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

        @Test
        fun `does not repeat the word tag in the placeholder of the plain Tag category`() {
            // ----- Arrange -----
            val category = TagCategory.TAG

            // ----- Act -----
            val field = tagNamingFieldFor(
                input = "",
                category = category,
                addPill = addPill,
            )

            // ----- Assert -----
            field.placeholder.contains(
                "Tag tag",
                ignoreCase = true,
            ) shouldBe false
        }
    }

    @Nested
    inner class WithTagDraft {
        @Test
        fun `stores the draft and mirrors it onto the naming field text`() {
            // ----- Arrange -----
            val state = BookDetailUiState()

            // ----- Act -----
            val result = state.withTagDraft(input = "slow burn")

            // ----- Assert -----
            result.tagEditorInput shouldBe "slow burn"
            result.tagNamingField.text shouldBe "slow burn"
        }

        @Test
        fun `keeps the naming field placeholder and variant`() {
            // ----- Arrange -----
            val state = BookDetailUiState()

            // ----- Act -----
            val result = state.withTagDraft(input = "slow burn")

            // ----- Assert -----
            result.tagNamingField.placeholder shouldBe state.tagNamingField.placeholder
            result.tagNamingField.variant shouldBe state.tagNamingField.variant
        }

        @Test
        fun `clears the naming field text when the draft is emptied`() {
            // ----- Arrange -----
            val state = BookDetailUiState().withTagDraft(input = "slow burn")

            // ----- Act -----
            val result = state.withTagDraft(input = "")

            // ----- Assert -----
            result.tagNamingField.text shouldBe ""
        }
    }
}
