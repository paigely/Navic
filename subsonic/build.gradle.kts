@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.kotlinMultiplatformLibrary)
	alias(libs.plugins.kotlin.serialization)
}

group = "paige"

kotlin {
	androidLibrary {
		namespace = "paige.subsonic"
		compileSdk = libs.versions.android.compileSdk.get().toInt()
		minSdk = libs.versions.android.minSdk.get().toInt()
		compilerOptions {
			jvmTarget = JvmTarget.JVM_17
		}
	}
	iosArm64()
	iosSimulatorArm64()

	sourceSets {
		commonMain.dependencies {
			implementation(libs.ktor.client.core)
			implementation(libs.ktor.client.content.negotiation)
			implementation(libs.ktor.serialization.json)
			implementation(libs.hash.md)
		}
		androidMain.dependencies {
			implementation(libs.ktor.client.okhttp)
		}
		iosMain.dependencies {
			implementation(libs.ktor.client.darwin)
		}
	}
}
