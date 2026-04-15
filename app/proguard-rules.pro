# Verbum - ProGuard Rules

# Keep Hilt generated classes
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Keep Moshi JSON adapters
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-keep class com.verbum.core.network.dto.** { *; }

# Keep Room entities
-keep class com.verbum.core.database.entity.** { *; }

# Keep Retrofit interfaces
-keep,allowobfuscation interface com.verbum.core.network.api.VerbumApi

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep data classes used for serialization
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
