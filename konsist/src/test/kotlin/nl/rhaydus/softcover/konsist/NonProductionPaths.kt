package nl.rhaydus.softcover.konsist

internal val NON_PRODUCTION_PATHS: List<String> = listOf(
    "smoketest/**",
    "**/src/debug/**",
    "**/src/test/**",
    "**/src/*Test/**",
)
