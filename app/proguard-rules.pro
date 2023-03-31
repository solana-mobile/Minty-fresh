# Keep OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
# ============ End of OkHttp platform ============

# =========== Log messages ===================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** w(...);
    public static *** v(...);
    public static *** i(...);
}
# =========== End log messages ===================

# ========== Prevent stripping constructors for GPA builders in metaplex library ==================
# https://github.com/metaplex-foundation/metaplex-android/blob/1.4.1/lib/src/main/java/com/metaplex/lib/shared/GpaBuilder.kt#L52
# Following code invokes constructor which is stripped by proguard.
-keep public class com.metaplex.lib.programs.token_metadata.gpa_builders.** {
  public protected *;
}