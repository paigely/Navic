import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

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
			if (buildType == NativeBuildType.RELEASE) {
				isStatic = false
				freeCompilerArgs += listOf(
					"-Xopt-in=kotlin.RequiresOptIn",
					"-Xdisable-phases=Devirtualization",
					"-Xdisable-phases=GlobalValueNumbering"
				)
				linkerOpts("-Wl,-dead_strip")
			} else {
				isStatic = true
			}
		}
	}

	sourceSets {
		commonMain.dependencies {
			implementation(project(":subsonic"))
			implementation(libs.composeMultiplatform.runtime)
			implementation(libs.composeMultiplatform.foundation)
			implementation(libs.composeMultiplatform.material3)
			implementation(libs.composeMultiplatform.material3.adaptive.navigation3)
			implementation(libs.composeMultiplatform.material3.windowSizeClass)
			implementation(libs.composeMultiplatform.ui)
			implementation(libs.composeMultiplatform.components.resources)
			implementation(libs.androidx.lifecycle.viewmodelCompose)
			implementation(libs.androidx.lifecycle.runtimeCompose)
			implementation(libs.coil.compose)
			implementation(libs.coil.network.ktor3)
			implementation(libs.capsule)
			implementation(libs.wavySlider)
			implementation(libs.ktor.serialization.json)
			implementation(libs.jetbrains.navigation3.ui)
			implementation(libs.kmpalette.core)
			implementation(libs.kmpalette.extensions.network)
			implementation(libs.ktor.client.core)
			implementation(libs.materialKolor)
			implementation(libs.multiplatformSettings.noArg)
			implementation(libs.multiplatformSettings.remember)
		}
		androidMain.dependencies {
			implementation(libs.androidx.activity.compose)
			implementation(libs.ktor.client.okhttp)
			implementation(libs.androidx.media3.exoplayer)
			implementation(libs.androidx.media3.session)
			implementation(libs.androidx.media3.ui)
		}
		iosMain.dependencies {
			implementation(libs.ktor.client.darwin
			)
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
			isDebuggable = false
			isProfileable = false
			isJniDebuggable = false
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}
