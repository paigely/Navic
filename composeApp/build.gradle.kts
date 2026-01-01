import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
}

kotlin {
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_11)
		}
	}

	listOf(
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "ComposeApp"
			isStatic = true
		}
	}

	sourceSets {
		androidMain.dependencies {
			implementation(libs.androidx.activity.compose)
			implementation(libs.coil.network.okhttp)
		}
		commonMain.dependencies {
			implementation(project(":subsonic"))
			implementation(libs.composeMultiplatform.runtime)
			implementation(libs.composeMultiplatform.foundation)
			implementation(libs.composeMultiplatform.material3)
			implementation(libs.composeMultiplatform.material3.windowSizeClass)
			implementation(libs.composeMultiplatform.ui)
			implementation(libs.composeMultiplatform.components.resources)
			implementation(libs.androidx.lifecycle.viewmodelCompose)
			implementation(libs.androidx.lifecycle.runtimeCompose)
			implementation(libs.coil.compose)
			implementation(libs.capsule)
			implementation(libs.wavySlider)
			implementation(libs.ktor.serialization.json)
			implementation(libs.jetbrains.navigation3.ui)
			implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")
			implementation("dev.burnoo:compose-remember-setting:1.0.3")
		}
	}
}

android {
	namespace = "paige.navic"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "paige.navic"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()
		versionCode = 1
		versionName = "1.0"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = true
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}
