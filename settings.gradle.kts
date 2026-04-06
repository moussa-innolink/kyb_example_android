pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // JitPack repository for KyvShield SDK
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "KyvShieldExample"

// Include the example app module
include(":app")

// Old local imports (uncomment for local development)
//
// include(":kyvshield")
// project(":kyvshield").projectDir = File("../kyvshield")
//
// include(":kyvshield-lite")
// project(":kyvshield-lite").projectDir = File("../kyvshield-lite")
