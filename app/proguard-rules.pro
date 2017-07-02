#-dontobfuscate

# Crashlytics 1.+
-keep class com.crashlytics.** { *; }
-keepattributes SourceFile,LineNumberTable

-dontwarn org.jetbrains.annotations.**

# OkHttp3
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

-dontwarn com.google.errorprone.annotations.*
