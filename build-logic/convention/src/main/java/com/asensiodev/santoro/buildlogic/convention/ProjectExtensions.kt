package com.asensiodev.buildlogic.convention.logic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>()
        .named("libs")

fun Project.getJavaVersion() =
    this.libs.findVersion(JAVA_VERSION).get().toString().toInt()

fun Project.getCompileSdk() =
    this.libs.findVersion(COMPILE_SDK).get().toString().toInt()

fun Project.getMinSdk() =
    this.libs.findVersion(MIN_SDK).get().toString().toInt()

fun Project.getTargetSdk() =
    this.libs.findVersion(TARGET_SDK).get().toString().toInt()

fun Project.getVersionCode() =
    this.libs.findVersion(VERSION_CODE).get().toString().toInt()

fun Project.getVersionName() =
    this.libs.findVersion(VERSION_NAME).get().toString()

private const val JAVA_VERSION = "javaVersion"
private const val COMPILE_SDK = "compileSdk"
private const val MIN_SDK = "minSdk"
private const val TARGET_SDK = "targetSdk"
private const val VERSION_CODE = "versionCode"
private const val VERSION_NAME = "versionName"
