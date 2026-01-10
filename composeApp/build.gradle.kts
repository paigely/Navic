import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.kotlin.serialization)
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
			implementation(libs.reorderable)
		}
		androidMain.dependencies {
			implementation(libs.androidx.activity.compose)
			implementation(libs.ktor.client.okhttp)
			implementation(libs.androidx.media3.exoplayer)
			implementation(libs.androidx.media3.session)
			implementation(libs.androidx.media3.ui)
		}
		iosMain.dependencies {
			implementation(
				libs.ktor.client.darwin
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
		versionName = "1.0.0-alpha02"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}

	signingConfigs {
		create("release") {
			keyAlias = System.getenv("SIGNING_KEY_ALIAS")
			keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
			storeFile = System.getenv("SIGNING_STORE_FILE")?.let(::File)
			storePassword = System.getenv("SIGNING_STORE_PASSWORD")
		}
	}

	buildTypes {
		val isRelease = System.getenv("RELEASE")?.toBoolean() ?: false
		val hasReleaseSigning = System.getenv("SIGNING_STORE_PASSWORD")?.isNotEmpty() == true

		if (isRelease && !hasReleaseSigning) {
			throw GradleException("Missing keystore in a release workflow!")
		}

		getByName("release") {
			isMinifyEnabled = true
			isDebuggable = false
			isProfileable = false
			isJniDebuggable = false
			isShrinkResources = true
			signingConfig = signingConfigs.getByName(if (hasReleaseSigning) "release" else "debug")
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}

	applicationVariants.all {
		outputs.all {
			val output = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
			val buildType = buildType.name
			val versionName = versionName ?: "unspecified"
			output.outputFileName = "navic-${buildType}-${versionName}.apk"
		}
	}

	androidComponents {
		onVariants(selector().withBuildType("release")) {
			it.packaging.resources.excludes.apply {
				// Debug metadata
				add("/**/*.version")
				add("/kotlin-tooling-metadata.json")
				// Kotlin debugging (https://github.com/Kotlin/kotlinx.coroutines/issues/2274)
				add("/DebugProbesKt.bin")
				// Reflection symbol list (https://stackoverflow.com/a/41073782/13964629)
				add("/**/*.kotlin_builtins")
			}
		}
	}

	packaging {
		resources {
			// okhttp3 is used by some lib (no cookies so publicsuffixes.gz can be dropped)
			excludes += "/okhttp3/**"

			// Remnants of smali/baksmali lib
			excludes += "/*.properties"
			excludes += "/org/antlr/**"
			excludes += "/com/android/tools/smali/**"
			excludes += "/org/eclipse/jgit/**"

			// bouncycastle
			excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
			excludes += "/org/bouncycastle/**"
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}