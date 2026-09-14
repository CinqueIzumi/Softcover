package nl.rhaydus.softcover.core.presentation.cover

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.PersistEditionImageUseCase
import nl.rhaydus.softcover.core.component.cover.CoverImagePersister
import nl.rhaydus.softcover.core.component.cover.LocalCoverImagePersister

/**
 * Resolves [PersistEditionImageUseCase] from Koin and provides it into [LocalCoverImagePersister]
 * as a [CoverImagePersister]. `:core:component` may not depend on Koin or on a domain use case
 * (`component-contract.md` § 7.4), so this is the seam that lets it persist a loaded cover's bytes
 * anyway, without ever knowing DI exists.
 *
 * This replaces the seam the old `EditionImage` in `:core:designsystem` used to leave open: it called
 * `koinInject<PersistEditionImageUseCase>()` directly from a component that did not compile against
 * the module providing that binding, resolving it only through the aggregate Koin graph. Mount this
 * once at the composition root — `:orchestration`'s `App` — rather than at each cover call site.
 */
@Composable
fun ProvideCoverImagePersister(content: @Composable () -> Unit) {
    val useCase = koinInject<PersistEditionImageUseCase>()

    val persister = remember(useCase) {
        CoverImagePersister { editionId, url, bytes ->
            useCase(
                editionId = editionId,
                url = url,
                bytes = bytes,
            ).onFailure { error ->
                AppLog.w("Cover image persist failed for edition $editionId: $error")
            }
        }
    }

    CompositionLocalProvider(
        LocalCoverImagePersister provides persister,
        content = content,
    )
}
