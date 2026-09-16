# Proguard rules for Job-Tracker Android App

# Keep Kotlinx Serialization models and companion objects
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <fields>;
}

# Keep shared models
-keep class com.rohit.jobtracker.shared.model.** { *; }

# Ktor and OkHttp
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
