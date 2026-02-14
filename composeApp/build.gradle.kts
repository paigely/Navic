
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.aboutLibraries)
	alias(libs.plugins.valkyrie)
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
			name = "Desktop"
			sourceFolder = "desktop"
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
	named("copyNonXmlValueResourcesForCommonMain") {
		dependsOn(":composeApp:exportLibraryDefinitions")
	}
	withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
		dependsOn(":composeApp:generateValkyrieImageVector")
	}
}

kotlin {
	@Suppress("DEPRECATION")
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_17)
		}
	}

	listOf(
		iosArm64(),
		iosSimulatorArm64()
	).forEach { target ->
		target.binaries.framework {
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

			implementation(libs.bundles.cmp)
			implementation(libs.bundles.ktor)
			implementation(libs.bundles.coil)
			implementation(libs.bundles.cmpThirdParty)
			implementation(libs.bundles.androidx.lifecycle)

			implementation(libs.navigation3.ui)
			implementation(libs.kotlinx.datetime)
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

		jvmMain.dependencies {
			implementation(libs.bundles.ktor.desktop)
			implementation(compose.desktop.currentOs)
			implementation(libs.kotlinx.coroutines.swing)
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

abstract class SyncComposeStringsTask : DefaultTask() {
	@get:InputDirectory
	abstract var baseDir: File

	@TaskAction
	fun sync() {
		val resourceDir = baseDir.resolve("src/commonMain/composeResources")
		val baseFile = resourceDir.resolve("values/strings.xml")

		val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		val baseDoc = documentBuilder.parse(baseFile).apply { documentElement.normalize() }

		val baseStrings = baseDoc.getElementsByTagName("string")
			.let { nodes ->
				(0 until nodes.length)
					.map { nodes.item(it) as Element }
					.associate { it.getAttribute("name") to it.textContent }
			}

		resourceDir.listFiles { f -> f.isDirectory && f.name.startsWith("values-") }?.forEach { localeDir ->
			val targetFile = localeDir.resolve("strings.xml")

			val existingStrings = if (targetFile.exists()) {
				val targetDoc = documentBuilder.parse(targetFile).apply { documentElement.normalize() }
				targetDoc.getElementsByTagName("string")
					.let { nodes -> (0 until nodes.length).map { nodes.item(it) as Element } }
					.associate { it.getAttribute("name") to it.textContent }
			} else {
				emptyMap()
			}

			val mergedStrings = baseStrings.keys
				.sorted()
				.associateWith {  existingStrings[it] ?: baseStrings.getValue(it) }

			val newDoc = documentBuilder.newDocument()
			val resources = newDoc.createElement("resources")
			newDoc.appendChild(resources)

			mergedStrings.forEach { (key, value) ->
				if (key !in existingStrings) {
					resources.appendChild(newDoc.createComment(" TODO: translate "))
				}
				resources.appendChild(newDoc.createElement("string").apply {
					setAttribute("name", key)
					textContent = value
				})
			}

			val transformer = TransformerFactory.newInstance().newTransformer().apply {
				setOutputProperty(OutputKeys.INDENT, "yes")
				setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
				setOutputProperty(OutputKeys.ENCODING, "utf-8")
				setOutputProperty(OutputKeys.METHOD, "xml")
				setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
			}

			transformer.transform(DOMSource(newDoc), StreamResult(targetFile))
			println("Updated: ${targetFile.path}")
		}
	}
}

tasks.register<SyncComposeStringsTask>("syncComposeStrings") {
	group = "localization"
	description = "Add missing strings to locale strings.xml"
	baseDir = project.projectDir
}