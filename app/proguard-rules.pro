# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Kotlin metadata
-keepattributes RuntimeVisibleAnnotations

# Keep Room entities
-keep class com.wikidoc.data.local.database.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Markdown libraries
-keep class org.commonmark.** { *; }

# Keep SMB library
-keep class jcifs.** { *; }

# Keep Coil
-keep class coil.** { *; }
