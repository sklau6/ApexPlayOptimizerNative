# Add project specific ProGuard rules here.
-keep class com.apexplayoptimizer.app.** { *; }

# Google AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# Google Play Billing
-keep class com.android.billingclient.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
