// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(Build.hiltAndroidGradlePlugin)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    }
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}