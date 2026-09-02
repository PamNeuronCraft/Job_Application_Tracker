import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
}

fun getSecret(key: String, default: String): String {
    return System.getenv(key) ?: project.findProperty(key)?.toString() ?: default
}

configure<com.android.build.api.dsl.ApplicationExtension> {
    namespace = "com.pamneuroncraft.jobapplicationtracker"
    compileSdk = 37

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(keystorePropertiesFile.inputStream())
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists() && keystoreProperties.containsKey("storeFile")) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
        getByName("debug") {
            if (keystorePropertiesFile.exists() && keystoreProperties.containsKey("debugStoreFile")) {
                storeFile = file(keystoreProperties.getProperty("debugStoreFile"))
                storePassword = keystoreProperties.getProperty("debugStorePassword")
                keyAlias = keystoreProperties.getProperty("debugKeyAlias")
                keyPassword = keystoreProperties.getProperty("debugKeyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.pamneuroncraft.jobapplicationtracker"
        minSdk = 24
        targetSdk = 37
        
        val versionMajor = 1
        val versionMinor = 5
        val buildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toInt() ?: 0
        
        versionCode = (versionMajor * 10000) + (versionMinor * 100) + buildNumber
        versionName = "$versionMajor.$versionMinor.$buildNumber"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            manifestPlaceholders["adMobAppId"] = "ca-app-pub-3940256099942544~3347511713"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["adMobAppId"] = getSecret("ADMOB_APP_ID_PROD", "ca-app-pub-9098088729873683~6121804769")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST,DEPENDENCIES}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared"))
    
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.perf)
    
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.play.services.ads)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.grpc") {
            useVersion("1.65.1")
        }
    }
}
