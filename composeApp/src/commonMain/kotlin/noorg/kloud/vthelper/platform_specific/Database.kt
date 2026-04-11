package noorg.kloud.vthelper.platform_specific

import androidx.room.BuiltInTypeConverters
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import noorg.kloud.vthelper.data.dbdaos.LecturerDao
import noorg.kloud.vthelper.data.dbdaos.LoggedInUserDao
import noorg.kloud.vthelper.data.dbdaos.ManoCourseDao
import noorg.kloud.vthelper.data.dbdaos.ManoSubjectTimetableDao
import noorg.kloud.vthelper.data.dbdaos.MoodleCourseDao
import noorg.kloud.vthelper.data.dbentities.DBLecturerEntity
import noorg.kloud.vthelper.data.dbentities.DBLoggedInUserEntity
import noorg.kloud.vthelper.data.dbentities.DBManoCourseEntity
import noorg.kloud.vthelper.data.dbentities.DBManoSubjectTimetableEntity
import noorg.kloud.vthelper.data.dbentities.DBMoodleCourseEntity

// https://developer.android.com/kotlin/multiplatform/room
// https://medium.com/@hidayatasep43/implementing-room-database-in-kotlin-multiplatform-a-step-by-step-guide-2bc3e1b3aa16
// https://proandroiddev.com/storing-data-in-local-database-like-a-boss-introducing-room-in-compose-multiplatform-2e39781c7b6a
// https://piashcse.medium.com/room-database-in-jetpack-compose-a-step-by-step-guide-for-android-development-6c7ae419105a

// https://medium.com/@majidshahbaz75/kotlin-flows-with-room-database-2d8b4b18790a
// https://medium.com/@kemal_codes/kotlin-flow-map-operator-3a2ad08bd953

@Database(
    entities = [
        DBMoodleCourseEntity::class,
        DBManoCourseEntity::class,
        DBManoSubjectTimetableEntity::class,
        DBLoggedInUserEntity::class,
        DBLecturerEntity::class
    ],
    version = 1,
)
@TypeConverters(
    builtInTypeConverters = BuiltInTypeConverters(
        enums = BuiltInTypeConverters.State.ENABLED,
        byteBuffer = BuiltInTypeConverters.State.ENABLED,
        uuid = BuiltInTypeConverters.State.ENABLED
    )
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodleCourseDao(): MoodleCourseDao
    abstract fun manoCourseDao(): ManoCourseDao
    abstract fun manoCourseTimetableDao(): ManoSubjectTimetableDao
    abstract fun loggedInUserDao(): LoggedInUserDao
    abstract fun lecturerDao(): LecturerDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

expect fun getDatabaseBuilder(dbName: String): RoomDatabase.Builder<AppDatabase>

fun getRoomDatabase(
    dbName: String
): AppDatabase {
    return getDatabaseBuilder(dbName)
        .addMigrations()
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}