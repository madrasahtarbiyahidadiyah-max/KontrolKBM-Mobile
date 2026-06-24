import java.net.URI
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.inspeksikbm.idadiyah"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  // implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

abstract class DownloadAppIconTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun download() {
        val baseDir = outputDir.get().asFile
        val destJpg = File(baseDir, "drawable/mmu_icon.jpg")
        val destXml = File(baseDir, "drawable/mmu_icon.xml")
        val destLogoPng = File(baseDir, "drawable/mmu_header_logo.png")

        // 1. Download Launcher Icon
        try {
            destJpg.parentFile.mkdirs()
            val bytes = URI("https://i.postimg.cc/PxzxVDf3/mm.jpg").toURL().readBytes()
            destJpg.writeBytes(bytes)
            if (destXml.exists()) {
                destXml.delete()
            }
            println("SUCCESS: App icon downloaded to ${destJpg.absolutePath}")
        } catch (e: Exception) {
            println("WARNING: Could not download app icon, creating fallback XML: ${e.message}")
            if (!destJpg.exists() && !destXml.exists()) {
                val fallbackXmlContent = """
                    <vector xmlns:android="http://schemas.android.com/apk/res/android"
                        android:width="108dp"
                        android:height="108dp"
                        android:viewportWidth="108"
                        android:viewportHeight="108">
                        <path android:fillColor="#046C4E" android:pathData="M15,15h78v78h-78z" />
                        <path android:fillColor="#FFFFFF" android:pathData="M34,28h40c2.2,0 4,1.8 4,4v48c0,2.2 -1.8,4 -4,4h-40c-2.2,0 -4,-1.8 -4,-4v-48c0,-2.2 1.8,-4 4,-4z" />
                        <path android:fillColor="#94A3B8" android:pathData="M46,24h16c1.1,0 2,0.9 2,2v4H44v-4c0,-1.1 0.9,-2 2,-2z" />
                        <path android:fillColor="#34D399" android:pathData="M40,40l4,4l8,-8l2,2l-10,10l-6,-6z" />
                        <path android:fillColor="#0F172A" android:pathData="M58,38h12v3h-12z" />
                        <path android:fillColor="#34D399" android:pathData="M40,54l4,4l8,-8l2,2l-10,10l-6,-6z" />
                        <path android:fillColor="#0F172A" android:pathData="M58,52h12v3h-12z" />
                        <path android:fillColor="#34D399" android:pathData="M40,68l4,4l8,-8l2,2l-10,10l-6,-6z" />
                        <path android:fillColor="#0F172A" android:pathData="M58,66h12v3h-12z" />
                    </vector>
                """.trimIndent()
                destXml.writeText(fallbackXmlContent)
            }
        }

        // 2. Download Header Logo
        try {
            val logoBytes = URI("https://i.postimg.cc/XNCQ2cCm/MMU-IDADIYAH.png").toURL().readBytes()
            destLogoPng.writeBytes(logoBytes)
            println("SUCCESS: Header logo downloaded to ${destLogoPng.absolutePath}")
        } catch (e: Exception) {
            println("WARNING: Could not download header logo, using launcher icon as fallback: ${e.message}")
            if (!destLogoPng.exists()) {
                if (destJpg.exists()) {
                    destJpg.copyTo(destLogoPng, overwrite = true)
                }
            }
        }
    }
}

tasks.register<DownloadAppIconTask>("downloadAppIcon") {
    outputDir.set(layout.projectDirectory.dir("src/main/res"))
}

tasks.configureEach {
    if (name.startsWith("preBuild") || name.startsWith("generate")) {
        dependsOn("downloadAppIcon")
    }
}

