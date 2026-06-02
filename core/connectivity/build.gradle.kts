plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.core.connectivity"
}

dependencies {
    api(project(":core:domain"))
    api(project(":core:database"))
    api(project(":core:book"))
    api(project(":core:lists"))
}
