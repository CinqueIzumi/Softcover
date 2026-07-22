package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice

// gender is persisted as its raw Int? id (see Gender.fromId) - consistent with how other
// unresolvable API enums are cached elsewhere in this app.
@Serializable
internal data class GenderSliceEntity(
    val genderId: Int? = null,
    val count: Int = 0,
    val fraction: Double = 0.0,
)

internal fun GenderSliceEntity.toModel(): GenderSlice = GenderSlice(
    gender = Gender.fromId(genderId),
    count = count,
    fraction = fraction,
)

internal fun GenderSlice.toEntity(): GenderSliceEntity = GenderSliceEntity(
    genderId = gender.id,
    count = count,
    fraction = fraction,
)
