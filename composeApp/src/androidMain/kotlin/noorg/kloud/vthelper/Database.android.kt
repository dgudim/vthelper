package noorg.kloud.vthelper

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import noorg.kloud.vthelper.data.AppDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("main.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}