plugins {
    kotlin("jvm") version "1.8.0" apply false    // Стабильная версия Kotlin
    id("com.android.application") version "8.1.1" apply false // Плагин для Android
}

allprojects {
    repositories {
        google()       // Для загрузки плагинов Android
        mavenCentral() // Для загрузки Kotlin и других зависимостей
    }
}
