// TOP LEVEL
plugins {
    id("com.android.application") version "8.1.1" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("com.google.gms:google-services:4.4.0")
        // ...
    }
}