package nl.rhaydus.softcover.showcase.live.field.action

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.showcase.live.field.event.FieldLiveEvent
import nl.rhaydus.softcover.showcase.live.field.mapper.fieldError
import nl.rhaydus.softcover.showcase.live.field.screenmodel.FieldLiveDependencies
import nl.rhaydus.softcover.showcase.live.field.state.FieldLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.field.state.FieldLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnFieldValueChangedAction(
    val key: String,
    val text: String,
) : FieldLiveAction {
    override suspend fun execute(
        dependencies: FieldLiveDependencies,
        scope: ActionScope<FieldLiveUiState, FieldLiveEvent, FieldLiveLocalVariables>,
    ) {
        scope.setState { state ->
            state.copy(
                fields = state.fields
                    .map {
                        if (it.field.key == key) {
                            it.copy(field = it.field.copy(
                                text = text,
                                error = fieldError(
                                    key = key,
                                    text = text,
                                ),
                            ),)
                        } else {
                            it
                        }
                    }
                    .toImmutableList(),
            )
        }
    }
}
