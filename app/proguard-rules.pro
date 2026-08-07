# =========================================================
# AdMob
# =========================================================
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# =========================================================
# Firebase (raw Android SDK bits: Analytics, Crashlytics)
# =========================================================
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }

# gitlive Firebase KMP wrappers (firebase-auth, firebase-firestore)
-keep class dev.gitlive.firebase.** { *; }
-keepclassmembers class dev.gitlive.firebase.** { *; }

# Firebase Firestore (native SDK, used under gitlive wrapper)
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.protobuf.** { *; }
-keep class io.grpc.** { *; }

# Firebase Auth (native SDK, used under gitlive wrapper)
-keep class com.google.firebase.auth.** { *; }

# =========================================================
# Google Credential Manager / Google Identity (Sign-In)
# =========================================================
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod

# =========================================================
# RevenueCat
# =========================================================
-keep class com.revenuecat.purchases.** { *; }

# =========================================================
# Kotlinx Serialization
# =========================================================
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod

# Keep generated $serializer companions for classes in your own package.
# ⚠️ Replace com.pamneuroncraft.jobapplicationtracker with your actual root package
# if it differs, and confirm this matches across all modules.
-keep,includedescriptorclasses class com.pamneuroncraft.jobapplicationtracker.**$$serializer { *; }
-keepclassmembers class com.pamneuroncraft.jobapplicationtracker.** {
    *** Companion;
}
-keepclasseswithmembers class com.pamneuroncraft.jobapplicationtracker.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
    <fields>;
}
-keep class kotlinx.serialization.json.** { *; }
-dontwarn kotlinx.serialization.**

# =========================================================
# Room (KMP, 2.8.x)
# =========================================================
-keep class **_Impl { *; }
# Room's own consumer rules (bundled in the AAR) cover most paging internals;
# don't hand-keep specific internal class names — they shift between versions.
-dontwarn androidx.room.paging.**
-dontwarn androidx.room.**

# =========================================================
# Paging3
# =========================================================
-dontwarn androidx.paging.**

# =========================================================
# Google Generative AI (Gemini)
# =========================================================
-keep class com.google.ai.client.generativeai.** { *; }

# =========================================================
# Koin
# =========================================================
# Koin's DSL is lambda-based (no classpath scanning/reflection for module
# registration), so it generally does NOT need a blanket keep. Test removing
# this once you've confirmed sign-in and other fixes work; re-add narrowly
# (e.g. -keep class org.koin.core.** { *; }) only if you see runtime errors.
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# =========================================================
# Ktor
# =========================================================
# Prefer narrow keeps over a blanket "keep everything" — Ktor's own artifacts
# ship consumer rules for most of this. Confirm your actual engine (OkHttp is
# not declared in commonMain here, so check androidMain/build.gradle.kts).
-dontwarn io.ktor.**
-keep class io.ktor.client.engine.okhttp.** { *; }
-keep class io.ktor.serialization.kotlinx.** { *; }
-keep class io.ktor.client.plugins.contentnegotiation.** { *; }

# =========================================================
# OkHttp & gRPC (transitive, used by Firestore / Ktor engine)
# =========================================================
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn com.squareup.okhttp.**
-dontwarn org.conscrypt.**
-dontwarn io.grpc.**

# =========================================================
# multiplatform-settings
# =========================================================
-dontwarn com.russhwolf.settings.**

# =========================================================
# General KMP safety nets
# =========================================================
-dontwarn kotlin.reflect.jvm.internal.**
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlin.Metadata