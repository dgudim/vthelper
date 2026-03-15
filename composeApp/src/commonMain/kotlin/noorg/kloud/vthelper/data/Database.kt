package noorg.kloud.vthelper.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import noorg.kloud.vthelper.data.daos.ManoCourseTimetableDao
import noorg.kloud.vthelper.data.daos.MoodleCourseDao
import noorg.kloud.vthelper.data.entities.ManoCourseTimetableEntity
import noorg.kloud.vthelper.data.entities.MoodleCourseEntity

// https://developer.android.com/kotlin/multiplatform/room

@Database(
    entities = [
        MoodleCourseEntity::class,
        ManoCourseTimetableEntity::class
    ],
    version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodleCourseDao(): MoodleCourseDao
    abstract fun manoCourseTimetableDao(): ManoCourseTimetableDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}