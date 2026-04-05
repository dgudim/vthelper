package noorg.kloud.vthelper.data.dbdaos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import noorg.kloud.vthelper.data.dbentities.DBLoggedInUserEntity

@Dao
interface LoggedInUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DBLoggedInUserEntity)

    @Query("DELETE FROM mano_timetable")
    suspend fun delete()

    @Query("SELECT * FROM logged_in_user LIMIT 1")
    suspend fun get(): DBLoggedInUserEntity?
}