package nl.rhaydus.softcover.core.domain.account

import nl.rhaydus.softcover.core.domain.model.RefreshScope

interface RefreshLibraryUseCase {
    suspend operator fun invoke(scope: RefreshScope = RefreshScope.All): Result<Unit>
}
