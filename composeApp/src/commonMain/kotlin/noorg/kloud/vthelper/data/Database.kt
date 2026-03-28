package noorg.kloud.vthelper.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import noorg.kloud.vthelper.data.dbdaos.LecturerDao
import noorg.kloud.vthelper.data.dbdaos.LoggedInUserDao
import noorg.kloud.vthelper.data.dbdaos.ManoCourseDao
import noorg.kloud.vthelper.data.dbdaos.ManoCourseTimetableDao
import noorg.kloud.vthelper.data.dbdaos.MoodleCourseDao
import noorg.kloud.vthelper.data.dbentities.LecturerEntity
import noorg.kloud.vthelper.data.dbentities.LoggedInUserEntity
import noorg.kloud.vthelper.data.dbentities.ManoCourseEntity
import noorg.kloud.vthelper.data.dbentities.ManoCourseTimetableEntity
import noorg.kloud.vthelper.data.dbentities.MoodleCourseEntity

// https://developer.android.com/kotlin/multiplatform/room

@Database(
    entities = [
        MoodleCourseEntity::class,
        ManoCourseEntity::class,
        ManoCourseTimetableEntity::class,
        LoggedInUserEntity::class,
        LecturerEntity::class
    ],
    version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodleCourseDao(): MoodleCourseDao
    abstract fun manoCourseDao(): ManoCourseDao
    abstract fun manoCourseTimetableDao(): ManoCourseTimetableDao
    abstract fun loggedInUserDao(): LoggedInUserDao
    abstract fun lecturerDao(): LecturerDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}