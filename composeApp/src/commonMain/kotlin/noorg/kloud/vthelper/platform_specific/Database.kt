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
import noorg.kloud.vthelper.data.dbdaos.mano.ManoEmployeeDao
import noorg.kloud.vthelper.data.dbdaos.LoggedInUserDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSemesterDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSubjectDao
import noorg.kloud.vthelper.data.dbdaos.mano.ManoSubjectTimetableDao
import noorg.kloud.vthelper.data.dbdaos.moodle.MoodleCourseDao
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeEntity
import noorg.kloud.vthelper.data.dbentities.DBLoggedInUserEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSemesterEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSettlementGroup
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectEntity
import noorg.kloud.vthelper.data.dbentities.mano.DBManoSubjectTimetableEntity
import noorg.kloud.vthelper.data.dbentities.moodle.DBMoodleCourseEntity
import noorg.kloud.vthelper.data.dbentities.mano.DbManoSettlementGrade

// https://developer.android.com/kotlin/multiplatform/room
// https://medium.com/@hidayatasep43/implementing-room-database-in-kotlin-multiplatform-a-step-by-step-guide-2bc3e1b3aa16
// https://proandroiddev.com/storing-data-in-local-database-like-a-boss-introducing-room-in-compose-multiplatform-2e39781c7b6a
// https://piashcse.medium.com/room-database-in-jetpack-compose-a-step-by-step-guide-for-android-development-6c7ae419105a

// https://medium.com/@majidshahbaz75/kotlin-flows-with-room-database-2d8b4b18790a
@Database(
    entities = [
        DBMoodleCourseEntity::class,
        DBManoSubjectEntity::class,
        DBManoSemesterEntity::class,
        DBManoSettlementGroup::class,
        DbManoSettlementGrade::class,
        DBManoSubjectTimetableEntity::class,
        DBManoEmployeeEntity::class,
        DBLoggedInUserEntity::class,
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
    abstract fun manoCourseDao(): ManoSubjectDao
    abstract fun manoSemesterDao(): ManoSemesterDao
    abstract fun manoEmployeeDao(): ManoEmployeeDao
    abstract fun manoCourseTimetableDao(): ManoSubjectTimetableDao
    abstract fun loggedInUserDao(): LoggedInUserDao
}

// https://medium.com/@kemal_codes/kotlin-flow-map-operator-3a2ad08bd953

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