package noorg.kloud.vthelper.data.dbdaos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.DBLoggedInUserEntity

@Dao
interface LoggedInUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replace(item: DBLoggedInUserEntity)

    @Query("DELETE FROM logged_in_user")
    suspend fun deleteAll()

    @Query("SELECT * FROM logged_in_user")
    fun getAllAsFlow(): Flow<List<DBLoggedInUserEntity>>

    @Query("SELECT * FROM logged_in_user")
    suspend fun getAll(): List<DBLoggedInUserEntity>
}