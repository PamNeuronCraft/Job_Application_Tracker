import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.android.kotlin.multiplatform)
}

fun getSecret(key: String, default: String): String {
    return System.getenv(key) ?: project.findProperty(key)?.toString() ?: default
}

buildkonfig {
    packageName = "com.pamneuroncraft.jobapplicationtracker"
    objectName = "AppBuildKonfig"
    
    defaultConfigs {
        buildConfigField(BOOLEAN, "IS_DEBUG", getSecret("IS_DEBUG", "true"))
        buildConfigField(BOOLEAN, "FEATURE_AI_IMPORT", "true")
        buildConfigField(BOOLEAN, "FEATURE_GOOGLE_DRIVE_BACKUP", "true")
        buildConfigField(BOOLEAN, "FEATURE_SUMMARY", "true")
        
        // Google Web Client IDs
        buildConfigField(STRING, "GOOGLE_WEB_CLIENT_ID_DEBUG", getSecret("GOOGLE_WEB_CLIENT_ID_DEBUG", "587001402052-idcta36ao4seblo39mas8q57vaabi7l9.apps.googleusercontent.com"))
        buildConfigField(STRING, "GOOGLE_WEB_CLIENT_ID_RELEASE", getSecret("GOOGLE_WEB_CLIENT_ID_RELEASE", "621221034219-9ue6l7p9v1gfeqrppgbtf884470jt91q.apps.googleusercontent.com"))
        
        // RevenueCat API Keys
        buildConfigField(STRING, "REVENUECAT_API_KEY_ANDROID_DEBUG", getSecret("REVENUECAT_API_KEY_ANDROID_DEBUG", "test_yIMjzBcWtbQriwjXQEvrlZYHJZN"))
        buildConfigField(STRING, "REVENUECAT_API_KEY_ANDROID_RELEASE", getSecret("REVENUECAT_API_KEY_ANDROID_RELEASE", "goog_HrWqyESiTxrpzpqsfSlKXUrGmEm"))
        buildConfigField(STRING, "REVENUECAT_API_KEY_IOS_DEBUG", getSecret("REVENUECAT_API_KEY_IOS_DEBUG", "test_yIMjzBcWtbQriwjXQEvrlZYHJZN"))
        buildConfigField(STRING, "REVENUECAT_API_KEY_IOS_RELEASE", getSecret("REVENUECAT_API_KEY_IOS_RELEASE", "appl_placeholder"))
        
        // Defaults to Test IDs
        buildConfigField(STRING, "ADMOB_APP_ID_DEBUG", "ca-app-pub-3940256099942544~3347511713")
        buildConfigField(STRING, "ADMOB_BANNER_UNIT_ID_DEBUG", "ca-app-pub-3940256099942544/6300978111")
        buildConfigField(STRING, "GEMINI_API_KEY_DEBUG", getSecret("GEMINI_API_KEY_DEV", ""))

        // Release IDs
        buildConfigField(STRING, "ADMOB_APP_ID_RELEASE", getSecret("ADMOB_APP_ID_PROD", "ca-app-pub-9098088729873683~6121804769"))
        buildConfigField(STRING, "ADMOB_BANNER_UNIT_ID_RELEASE", getSecret("ADMOB_BANNER_UNIT_ID_PROD", "ca-app-pub-9098088729873683/1918835878"))
        buildConfigField(STRING, "GEMINI_API_KEY_RELEASE", getSecret("GEMINI_API_KEY_PROD", ""))
    }
}

kotlin {
    android {
        namespace = "com.pamneuroncraft.jobapplicationtracker.shared"
        compileSdk = 37
        minSdk = 24
        
        androidResources {
            enable = true
        }
        
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.components.ui.tooling.preview)
            implementation(libs.compose.ui.tooling.preview)

            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.paging)
            implementation(libs.androidx.sqlite.bundled)
            
            implementation(libs.navigation.compose)
            
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.multiplatform.settings)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.paging.compose)
            implementation(libs.purchases.core)
            implementation(libs.uuid)
            
            implementation(libs.adaptive)
            implementation(libs.adaptive.layout)
            implementation(libs.adaptive.navigation)
            implementation(libs.adaptive.suite)
        }
        
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)

            api(libs.androidx.activity.compose)
            api(libs.androidx.core.ktx)
            api(libs.androidx.lifecycle.runtime.ktx)
            
            api(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            
            implementation(libs.generativeai)
            implementation(libs.jsoup)
            implementation(libs.play.services.ads)
            
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.androidx.paging.runtime)
            implementation(libs.play.review)
            implementation(libs.play.review.ktx)
            implementation(libs.play.update)
            implementation(libs.play.update.ktx)
            
            implementation(libs.google.gmail)
            implementation(libs.google.api.client)
            implementation(libs.google.auth.oauth2)
            implementation(libs.gms.play.services.auth)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    //add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

compose.resources {
    packageOfResClass = "com.pamneuroncraft.jobapplicationtracker.shared"
}
