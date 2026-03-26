# Keep Room entities and generated adapters.
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep Hilt generated classes.
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class * extends dagger.hilt.internal.GeneratedComponentManager

# Keep kotlinx serialization metadata used by reflection-free compose tooling.
-keep class kotlin.Metadata { *; }

# Keep enum names for persisted values in DB.
-keepclassmembers enum com.dadnavigator.app.domain.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
