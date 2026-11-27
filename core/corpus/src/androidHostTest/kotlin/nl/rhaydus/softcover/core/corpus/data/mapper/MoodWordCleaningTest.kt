package nl.rhaydus.softcover.core.corpus.data.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodWordCleaningTest {
    @Nested
    inner class ClampedStrength {
        @Test
        fun `null stays null`() {
            // ----- Arrange -----
            val strength: Int? = null

            // ----- Act -----
            val result = strength.clampedStrength()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `values past either end are pulled into zero to one hundred`() {
            // ----- Arrange -----
            val inputs = listOf(-5, 0, 55, 100, 250)

            // ----- Act -----
            val result = inputs.map { it.clampedStrength() }

            // ----- Assert -----
            result shouldBe listOf(0, 0, 55, 100, 100)
        }
    }

    @Nested
    inner class Cleaned {
        @Test
        fun `an own word is trimmed`() {
            // ----- Arrange -----
            val word = MoodWord.Own(text = "  Wistful \n")

            // ----- Act -----
            val result = word.cleaned()

            // ----- Assert -----
            result shouldBe MoodWord.Own(text = "Wistful")
        }

        @Test
        fun `a whitespace-only own word is dropped`() {
            // ----- Arrange -----
            val word = MoodWord.Own(text = " \t\n ")

            // ----- Act -----
            val result = word.cleaned()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `a preset is left as it is`() {
            // ----- Arrange -----
            val word = MoodWord.Preset(mood = Mood.Dark)

            // ----- Act -----
            val result = word.cleaned()

            // ----- Assert -----
            result shouldBe word
        }
    }

    @Nested
    inner class IsAllowedIn {
        @Test
        fun `a preset is allowed only when it is on the list`() {
            // ----- Arrange -----
            val onList = MoodWord.Preset(mood = Mood.Curious)
            val offList = MoodWord.Preset(mood = Mood.Cosy)

            // ----- Act -----
            val results = listOf(onList, offList).map { it.isAllowedIn(presets = Mood.Moment) }

            // ----- Assert -----
            results shouldBe listOf(true, false)
        }

        @Test
        fun `an own word is allowed against any list`() {
            // ----- Arrange -----
            val word = MoodWord.Own(text = "Anything")

            // ----- Act -----
            val result = word.isAllowedIn(presets = emptyList())

            // ----- Assert -----
            result shouldBe true
        }
    }
}
