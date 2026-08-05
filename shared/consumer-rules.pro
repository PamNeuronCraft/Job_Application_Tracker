# Kotlinx Serialization
-keepattributes *Annotation*, EnclosingMethod, InnerClasses
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
    <fields>;
}
-keep class kotlinx.serialization.json.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class **_Impl { *; }
-keep class androidx.room.paging.LimitOffsetPagingSource { *; }

# Firebase Common
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.protobuf.** { *; }
-keep class io.grpc.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Google Generative AI (Gemini)
-keep class com.google.ai.client.generativeai.** { *; }

# Koin
-keep class org.koin.** { *; }

# Ktor
-keep class io.ktor.** { *; }
-dontwarn java.lang.management.**

# OkHttp & gRPC
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn com.squareup.okhttp.**
-dontwarn org.conscrypt.**
