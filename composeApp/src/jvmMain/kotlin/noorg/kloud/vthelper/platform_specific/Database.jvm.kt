package noorg.kloud.vthelper.platform_specific

import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(dbName: String): RoomDatabase.Builder<AppDatabase> {
    val dbFile = appDataDirectory() / dbName
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.toString(),
    )
}