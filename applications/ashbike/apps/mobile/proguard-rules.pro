# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Hilt
-keep,allowobfuscation,allowshrinking class dagger.hilt.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { *; }

# Maps
-keep class com.google.android.gms.maps.** { *; }

# Health Connect
-keep class androidx.health.connect.client.records.** { *; }

# Serialization & Nav3 Destination Keys
-keep class * extends kotlinx.serialization.KSerializer { *; }
-keep @kotlinx.serialization.Serializable class com.zoewave.probase.ashbike.features.main.navigation.** { *; }
-keepclassmembers class com.zoewave.probase.ashbike.features.main.navigation.** {
    public static final ** Companion;
    public static ** serializer(...);
}

# BLE Hub & Decoders
-keep class com.zoewave.ashbike.features.ble.** { *; }