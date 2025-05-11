// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        // Le repo ObjectBox
        maven("https://objectbox.io/maven")
    }
    dependencies {
        // Classpath du plugin Gradle ObjectBox
        classpath("io.objectbox:objectbox-gradle-plugin:4.2.0")
    }
}



plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
}