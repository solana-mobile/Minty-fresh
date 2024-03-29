pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
rootProject.name = "Minty Fresh"
include(":app")
include(":ui:commonComposable")
include(":ui:gallery")
include(":ui:mymints")
include(":libs:persistence")
include(":libs:mintycore")
include(":ui:walletConnectButton")
include(":ui:nftMint")
include(":libs:networkConfigs")
include(":libs:networkInterface")
include(":libs:networkInterfaceImpl")
include(":ui:settings")
