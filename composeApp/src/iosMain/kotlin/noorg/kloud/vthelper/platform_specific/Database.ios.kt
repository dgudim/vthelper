package noorg.kloud.vthelper.platform_specific

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun getDatabaseBuilder(dbName: String): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = appDataDirectory() / dbName
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath.toString(),
    )
}