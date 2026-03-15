package noorg.kloud.vthelper.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.entities.LoggedInUserEntity

@Dao
interface LoggedInUserDao {
    @Insert
    suspend fun insert(item: LoggedInUserEntity)

    @Query("SELECT count(*) > 0 FROM mano_timetable")
    suspend fun hasUser(): Int

    @Query("SELECT * FROM logged_in_user LIMIT 1")
    suspend fun get(): LoggedInUserEntity
}