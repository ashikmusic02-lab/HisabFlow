# Keep Room database and DAOs
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep our entities and DAOs
-keep class com.example.data.local.** { *; }
-keep class com.example.model.** { *; }

# Keep Moshi and Kotlin serialization
-keep class com.squareup.moshi.** { *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
