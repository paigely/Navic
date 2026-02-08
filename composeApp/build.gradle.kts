import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.aboutLibraries)
}
aboutLibraries {
	collect {
		configPath = file("acknowledgements")
	}
	export {
		outputFile = file("src/commonMain/composeResources/files/acknowledgements.json")
	}
}

tasks.named("copyNonXmlValueResourcesForCommonMain") {
	dependsOn(":composeApp:exportLibraryDefinitions")
}

kotlin {
	@Suppress("DEPRECATION")
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_17)
		}
	}

	iosArm64 {
		binaries.framework {
			baseName = "ComposeApp"
			isStatic = true
		}
	}

	jvm {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_17)
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
			implementation(libs.composePipette)
			implementation(libs.materialKolor)
			implementation(libs.ktor.client.content.negotiation)
			implementation(libs.ktor.serialization.json)
			implementation(libs.jetbrains.navigation3.ui)
			implementation(libs.kmpalette.core)
			implementation(libs.kmpalette.extensions.network)
			implementation(libs.ktor.client.core)
			implementation(libs.multiplatformSettings.noArg)
			implementation(libs.reorderable)
			implementation(libs.aboutLibraries.core)
			implementation(libs.aboutLibraries.compose.m3)
		}
		androidMain.dependencies {
			implementation(libs.androidx.activity.compose)
			implementation(libs.ktor.client.okhttp)
			implementation(libs.androidx.media3.exoplayer)
			implementation(libs.androidx.media3.session)
			implementation(libs.androidx.media3.ui)
			implementation(libs.androidx.animation.graphics)
		}
		iosMain.dependencies {
			implementation(
				libs.ktor.client.darwin
			)
		}
		jvmMain.dependencies {
			implementation(compose.desktop.currentOs)
			implementation(libs.kotlinx.coroutinesSwing)
			implementation(libs.ktor.client.okhttp)
			implementation(libs.nativeTray)
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
		versionCode = 6
		versionName = "1.0.0-alpha16"
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
				getDefaultProguardFile("proguard-android-optimize.txt")
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
				add("/**/*.version")
				add("/kotlin-tooling-metadata.json")
				add("/DebugProbesKt.bin")
				add("/**/*.kotlin_builtins")
			}
		}
	}

	packaging {
		resources {
			excludes += "/okhttp3/**"
			excludes += "/*.properties"
			excludes += "/org/antlr/**"
			excludes += "/com/android/tools/smali/**"
			excludes += "/org/eclipse/jgit/**"
			excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
			excludes += "/org/bouncycastle/**"
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}

compose.desktop {
	application {
		mainClass = "paige.navic.MainKt"
		nativeDistributions {
			targetFormats(TargetFormat.Msi, TargetFormat.Dmg, TargetFormat.AppImage)
			packageName = "paige.Navic"
			linux.iconFile = project.file("../.github/icon.png")
		}
	}
}
