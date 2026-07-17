package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice

@Serializable
internal data class GenreSliceEntity(
    val name: String = "",
    val count: Int = 0,
    val fraction: Double = 0.0,
)

internal fun GenreSliceEntity.toModel(): GenreSlice = GenreSlice(
    name = name,
    count = count,
    fraction = fraction,
)

internal fun GenreSlice.toEntity(): GenreSliceEntity = GenreSliceEntity(
    name = name,
    count = count,
    fraction = fraction,
)
