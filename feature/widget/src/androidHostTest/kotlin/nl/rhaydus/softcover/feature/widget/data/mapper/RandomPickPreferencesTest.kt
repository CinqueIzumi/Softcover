package nl.rhaydus.softcover.feature.widget.data.mapper

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettings
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RandomPickPreferencesTest {
    @Nested
    inner class Settings {
        @Test
        fun `an unknown stored cadence falls back to daily`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf(stringPreferencesKey("random_pick_cadence") to "Fortnightly")

            // ----- Act -----
            val result = preferences.toRandomPickSettings()

            // ----- Assert -----
            result.cadence shouldBe RandomPickCadence.Daily
        }

        @Test
        fun `an unknown stored source kind falls back to want to read`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf(stringPreferencesKey("random_pick_source_kind") to "mystery")

            // ----- Act -----
            val result = preferences.toRandomPickSettings()

            // ----- Assert -----
            result.source shouldBe RandomPickSource.WantToRead
        }

        @Test
        fun `a user list source reads back with the same id`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()
            val settings = RandomPickSettings(
                source = RandomPickSource.UserList(listId = 42),
                cadence = RandomPickCadence.Weekly,
            )

            // ----- Act -----
            preferences.write(settings)

            // ----- Assert -----
            preferences.toRandomPickSettings() shouldBe settings
        }

        @Test
        fun `owned source reads back`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()
            val settings = RandomPickSettings(
                source = RandomPickSource.Owned,
                cadence = RandomPickCadence.OnShuffleOnly,
            )

            // ----- Act -----
            preferences.write(settings)

            // ----- Assert -----
            preferences.toRandomPickSettings() shouldBe settings
        }
    }

    @Nested
    inner class State {
        @Test
        fun `a written pick reads back`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()
            val state = RandomPickState(
                pickedBookId = 7,
                pickedAtMillis = 1_700_000_000_000L,
            )

            // ----- Act -----
            preferences.write(state)

            // ----- Assert -----
            preferences.toRandomPickState() shouldBe state
        }

        @Test
        fun `writing an empty state clears a stored pick`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()
            preferences.write(
                RandomPickState(
                    pickedBookId = 7,
                    pickedAtMillis = 1L,
                ),
            )

            // ----- Act -----
            preferences.write(RandomPickState())

            // ----- Assert -----
            preferences.toRandomPickState() shouldBe RandomPickState()
        }
    }
}
