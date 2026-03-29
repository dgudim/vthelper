package noorg.kloud.vthelper.data.dbdaos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import noorg.kloud.vthelper.data.dbentities.DBLoggedInUserEntity

@Dao
interface LoggedInUserDao {
    @Insert
    suspend fun insert(item: DBLoggedInUserEntity)

    @Query("SELECT count(*) > 0 FROM mano_timetable")
    suspend fun hasUser(): Int

    @Query("SELECT * FROM logged_in_user LIMIT 1")
    suspend fun get(): DBLoggedInUserEntity
}