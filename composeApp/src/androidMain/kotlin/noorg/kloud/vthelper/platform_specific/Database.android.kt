package noorg.kloud.vthelper.platform_specific

import androidx.room.Room
import androidx.room.RoomDatabase
import noorg.kloud.vthelper.applicationContext

actual fun getDatabaseBuilder(dbName: String): RoomDatabase.Builder<AppDatabase> {
    val dbFile = applicationContext.getDatabasePath(dbName)
    return Room.databaseBuilder<AppDatabase>(
        context = applicationContext,
        name = dbFile.absolutePath
    )
}