package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.TagCategory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShareTagPickerBuilderTest {
    private val candidates = mapOf(
        TagCategory.GENRE to listOf("Fantasy", "Mystery"),
        TagCategory.MOOD to listOf("Cozy"),
    )

    @Nested
    inner class ShareTagPickerFor {
        @Test
        fun `is absent when there are no candidates`() {
            // ----- Arrange -----
            val noCandidates = emptyMap<TagCategory, List<String>>()

            // ----- Act -----
            val result = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = noCandidates,
                selected = emptySet(),
                expanded = false,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `counts the selected candidates in the header label`() {
            // ----- Arrange -----
            val selected = setOf("Fantasy", "Cozy")

            // ----- Act -----
            val result = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = candidates,
                selected = selected,
                expanded = false,
            ).shouldNotBeNull()

            // ----- Assert -----
            result.header.label shouldBe "Tags · 2"
        }

        @Test
        fun `labels one selected tag as Tags · 1`() {
            // ----- Arrange -----
            val selected = setOf("Mystery")

            // ----- Act -----
            val result = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = candidates,
                selected = selected,
                expanded = false,
            ).shouldNotBeNull()

            // ----- Assert -----
            result.header.label shouldBe "Tags · 1"
        }

        @Test
        fun `labels no selection as Tags`() {
            // ----- Arrange -----
            val selected = emptySet<String>()

            // ----- Act -----
            val result = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = candidates,
                selected = selected,
                expanded = false,
            ).shouldNotBeNull()

            // ----- Assert -----
            result.header.label shouldBe "Tags"
        }

        @Test
        fun `does not count selected names that are not candidates`() {
            // ----- Arrange -----
            val selected = setOf("Fantasy", "Removed tag")

            // ----- Act -----
            val result = shareTagPickerFor(
                variant = ShareCardVariant.MY_UPDATE,
                candidates = candidates,
                selected = selected,
                expanded = false,
            ).shouldNotBeNull()

            // ----- Assert -----
            result.header.label shouldBe "Tags · 1"
        }

        @Test
        fun `counts a name shared by two categories once`() {
            // ----- Arrange -----
            val shared = mapOf(
                TagCategory.GENRE to listOf("Dark"),
                TagCategory.MOOD to listOf("Dark"),
            )

            // ----- Act -----
            val result = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = shared,
                selected = setOf("Dark"),
                expanded = false,
            ).shouldNotBeNull()

            // ----- Assert -----
            result.header.label shouldBe "Tags · 1"
        }

        @Test
        fun `groups the candidates by category label and marks the selected chips`() {
            // ----- Arrange -----
            val selected = setOf("Mystery")

            // ----- Act -----
            val result = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = candidates,
                selected = selected,
                expanded = true,
            ).shouldNotBeNull()

            // ----- Assert -----
            result.groups.map { (it.header as SectionHeaderUiModel.Label).eyebrow } shouldBe listOf("Genre", "Mood")
            result.groups.first().tags.chips.map { it.label to it.selected } shouldBe listOf(
                "Fantasy" to false,
                "Mystery" to true,
            )
        }

        @Test
        fun `carries the variant and the expanded flag`() {
            // ----- Arrange -----
            val expanded = true

            // ----- Act -----
            val result = shareTagPickerFor(
                variant = ShareCardVariant.MY_UPDATE,
                candidates = candidates,
                selected = emptySet(),
                expanded = expanded,
            ).shouldNotBeNull()

            // ----- Assert -----
            result.variant shouldBe ShareCardVariant.MY_UPDATE
            result.expanded shouldBe true
        }

        @Test
        fun `shows a hide disclosure while expanded`() {
            // ----- Arrange -----
            val expanded = true

            // ----- Act -----
            val result = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = candidates,
                selected = emptySet(),
                expanded = expanded,
            ).shouldNotBeNull()

            // ----- Assert -----
            result.header.trailing.shouldBeInstanceOf<ChipTrailing.Disclosure>().expanded shouldBe true
        }

        @Test
        fun `shows a collapsed disclosure while collapsed`() {
            // ----- Arrange -----
            val expanded = false

            // ----- Act -----
            val result = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = candidates,
                selected = emptySet(),
                expanded = expanded,
            ).shouldNotBeNull()

            // ----- Assert -----
            result.header.trailing.shouldBeInstanceOf<ChipTrailing.Disclosure>().expanded shouldBe false
        }

        @Test
        fun `does not mark the header chip as selected when tags are chosen`() {
            // ----- Arrange -----
            val selected = setOf("Fantasy")

            // ----- Act -----
            val result = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = candidates,
                selected = selected,
                expanded = true,
            ).shouldNotBeNull()

            // ----- Assert -----
            result.header.selected shouldBe false
        }
    }
}
