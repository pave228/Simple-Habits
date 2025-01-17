rootProject.name = "Habits"
include(":app")

pluginManagement {
    repositories {
        google()       // Репозиторий для плагинов Android
        mavenCentral() // Репозиторий для Kotlin и других зависимостей
    }
}

dependencyResolutionManagement {
    repositories {
        google()       // Репозиторий для плагинов Android
        mavenCentral() // Репозиторий для Kotlin и других зависимостей
    }
}
