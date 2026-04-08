import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.aboutLibraries)
	alias(libs.plugins.valkyrie)
	alias(libs.plugins.ksp)
	alias(libs.plugins.androidx.room)
}

configurations.all {
	exclude(group = "org.jetbrains.compose.material", module = "material")
	exclude(group = "androidx.compose.material", module = "material")
}

valkyrie {
	packageName = "paige.navic.icons"
	generateAtSync = true
	outputDirectory = layout.buildDirectory.dir("generated/sources/valkyrie")

	iconPack {
		name = "Icons"
		targetSourceSet = "commonMain"

		nested {
			name = "Brand"
			sourceFolder = "brand"
		}

		nested {
			name = "Outlined"
			sourceFolder = "outlined"
		}

		nested {
			name = "Filled"
			sourceFolder = "filled"
		}
	}
}

aboutLibraries {
	collect {
		configPath = file("acknowledgements")
	}
	export {
		outputFile = file("src/commonMain/composeResources/files/acknowledgements.json")
	}
}

tasks {
	matching { it.name.startsWith("ksp") }.configureEach {
		dependsOn(":composeApp:generateValkyrieImageVector")
	}
	named("copyNonXmlValueResourcesForCommonMain") {
		dependsOn(":composeApp:exportLibraryDefinitions")
	}
	withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
		dependsOn(":composeApp:generateValkyrieImageVector")
	}
	withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
		compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
	}
	matching { it.name.startsWith("compileKotlinIos") }.configureEach {
		// “truly horrifying workaround” for a crash in SearchScreen.kt
		// https://youtrack.jetbrains.com/issue/KT-84055/Reference-to-lambda-in-lambda-in-function-TextField-can-not-be-evaluated#focus=Comments-27-13188532.0-0
		val tmp = layout.buildDirectory.dir("generated/iosWorkaround/commonMain/kotlin").get()
		kotlin.sourceSets["commonMain"].kotlin.srcDir(tmp)

		doFirst {
			tmp.asFile.mkdirs()
			tmp.file("TextFieldDecorator.kt").asFile.writeText(
				"""
package androidx.compose.foundation.text.input

import androidx.compose.runtime.Composable

public fun interface TextFieldDecorator {
    @Suppress("ComposableLambdaParameterNaming")
    @Composable
    public fun Decoration(innerTextField: @Composable () -> Unit)
}
"""
			)
		}
		doLast {
			tmp.asFile.deleteRecursively()
		}
	}
}

kotlin {
	@Suppress("DEPRECATION")
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_21)
		}
	}

	listOf(
		iosArm64(),
		iosSimulatorArm64()
	).forEach {
		it.binaries.framework {
			baseName = "ComposeApp"
			isStatic = true
		}
	}

	sourceSets {
		commonMain.dependencies {
			implementation(libs.bundles.cmp)
			implementation(libs.bundles.ktor)
			implementation(libs.bundles.coil)
			implementation(libs.bundles.cmpThirdParty)
			implementation(libs.bundles.androidx.lifecycle)
			implementation(libs.bundles.room)
			implementation(libs.bundles.koin)

			implementation(libs.navigation3.ui)
			implementation(libs.kotlinx.datetime)
			implementation(libs.kotlinx.serialization.json)
			implementation(libs.androidx.datastore.preferences)
			implementation(libs.coil.gif)

			implementation(libs.subsonicKotlin)
		}

		androidMain.dependencies {
			implementation(libs.bundles.ktor.android)
			implementation(libs.bundles.androidx.android)
			implementation(libs.bundles.media3)
			implementation(libs.bundles.glance)
		}

		iosMain.dependencies {
			implementation(libs.bundles.ktor.ios)
		}
	}
}

dependencies {
	add("kspAndroid", libs.androidx.room.compiler)

	val isMacOs = System.getProperty("os.name").lowercase().contains("mac")
	if (isMacOs) {
		add("kspIosSimulatorArm64", libs.androidx.room.compiler)
		add("kspIosArm64", libs.androidx.room.compiler)
	}
}

android {
	namespace = "paige.navic"
	compileSdk = libs.versions.android.compileSdk.get().toInt()
	defaultConfig {
		applicationId = "paige.navic"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()
		versionCode = 21
		versionName = "v1.0.0-alpha31"
		ndk {
			abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
			val isRelease = System.getenv("RELEASE")?.toBoolean() ?: false
			if (!isRelease) {
				abiFilters.add("x86_64")
			}
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

		getByName("debug") {
			applicationIdSuffix = ".debug"
			resValue("string", "app_name", "Navic (Dev)")
		}
	}

	applicationVariants.all {
		outputs.all {
			val output = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
			output.outputFileName = "Navic.apk"
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
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
	buildToolsVersion = "37.0.0"
}

room {
	schemaDirectory("$projectDir/schemas")
}
