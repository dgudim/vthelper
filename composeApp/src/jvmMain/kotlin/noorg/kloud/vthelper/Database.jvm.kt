package noorg.kloud.vthelper

import androidx.room.Room
import androidx.room.RoomDatabase
import noorg.kloud.vthelper.data.AppDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.home") + "/.vthelper", "main.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}