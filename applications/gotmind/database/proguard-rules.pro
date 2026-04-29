# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep all Room database entities, DAOs, and TypeConverters.
-keep class com.zoewave.probase.gotmind.database.** { *; }
