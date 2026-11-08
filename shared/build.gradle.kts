import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

// ── Openinary (image hosting) keys ──────────────────────────────────────────
// Read from the git-ignored local.properties and compiled into an internal
// Kotlin object, so the API key never lands in the repository:
//   openinary.apiKey=oik_...
//   openinary.baseUrl=https://cdn.openinary.dev   (optional)
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val openinaryApiKey: String = localProps.getProperty("openinary.apiKey", "").trim()
val openinaryBaseUrl: String = localProps.getProperty("openinary.baseUrl", "https://cdn.openinary.dev").trim().trimEnd('/')
val openinaryGenDir = layout.buildDirectory.dir("generated/openinary/kotlin")
val generateOpeninaryKeys by tasks.registering {
    inputs.property("apiKey", openinaryApiKey)
    inputs.property("baseUrl", openinaryBaseUrl)
    outputs.dir(openinaryGenDir)
    val outDir = openinaryGenDir
    val key = openinaryApiKey
    val base = openinaryBaseUrl
    doLast {
        fun esc(v: String) = v.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\${'$'}")
        val dir = outDir.get().asFile.resolve("com/yallakhedma/app/config")
        dir.mkdirs()
        dir.resolve("OpeninaryKeys.kt").writeText(
            """
            |// GENERATED from local.properties — do not edit or commit.
            |package com.yallakhedma.app.config
            |
            |internal object OpeninaryKeys {
            |    const val API_KEY: String = "${esc(key)}"
            |    const val BASE_URL: String = "${esc(base)}"
            |}
            |""".trimMargin(),
        )
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    androidLibrary {
       namespace = "com.yallakhedma.app.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_17
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.androidDriver)
            implementation(libs.koin.android)
            implementation(libs.androidx.activity.compose)

            // Firebase Android BoM — required by GitLive Firebase SDK to resolve
            // transitive com.google.firebase:firebase-*-ktx dependencies.
            // Pinned to 32.x because BoM 33+ removed the -ktx artifacts that GitLive 2.1.0 references.
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:32.7.4"))

            // Google Sign-In via Credential Manager
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.playServicesAuth)
            implementation(libs.google.identity.googleid)
            // OTP email is sent server-side now (Cloud Functions) — the client
            // no longer needs JavaMail/SMTP.
        }
        commonMain {
            kotlin.srcDir(generateOpeninaryKeys)
        }
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinxJson)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)

            // Voyager
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.tabNavigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)

            // Coil 3 (images)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Settings (KV storage)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)
            implementation(libs.multiplatform.settings.serialization)

            // SQLDelight
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            // Firebase (GitLive)
            implementation(libs.firebase.common)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.functions)
            implementation(libs.firebase.messaging)
            implementation(libs.firebase.analytics)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.nativeDriver)
        }
    }
}

sqldelight {
    databases {
        create("YallaKhidmaDb") {
            packageName.set("com.yallakhedma.app.db")
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
