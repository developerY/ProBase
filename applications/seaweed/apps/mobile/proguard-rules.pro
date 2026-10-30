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
-keepnames @kotlinx.serialization.Serializable class com.zoewave.probase.seaweed.**
-keepclassmembers class com.zoewave.probase.seaweed.** {
    <fields>;
    <init>(...);
}

-keepnames @kotlinx.serialization.Serializable class com.zoewave.probase.features.ai.**
-keepclassmembers class com.zoewave.probase.features.ai.** {
    <fields>;
    <init>(...);
}

-keepnames @kotlinx.serialization.Serializable class com.zoewave.probase.kocolor.**
-keepclassmembers class com.zoewave.probase.kocolor.** {
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
