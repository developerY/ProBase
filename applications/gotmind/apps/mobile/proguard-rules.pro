# Kotlinx Serialization Rules
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @kotlinx.serialization.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation class * {
    @kotlinx.serialization.* <methods>;
    @kotlinx.serialization.* <fields>;
}

# Keep classes that are specifically serialized by name
-keepnames @kotlinx.serialization.Serializable class com.zoewave.probase.gotmind.**
-keepclassmembers class com.zoewave.probase.gotmind.** {
    <fields>;
    <init>(...);
}

# SQLCipher / SQLite JNI Rules
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-keep class androidx.sqlite.db.framework.** { *; }

# Google Tink Rules
-keep class com.google.crypto.tink.** { *; }
-keep class * extends com.google.crypto.tink.KeyTypeManager { *; }
-keep class * extends com.google.crypto.tink.PrimitiveWrapper { *; }

# Google API Client and Joda Time (Dependencies for Tink)
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**
