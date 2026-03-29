package noorg.kloud.vthelper.data.dbdaos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import noorg.kloud.vthelper.data.dbentities.DBLecturerEntity

@Dao
interface LecturerDao {
    @Insert
    suspend fun insert(item: DBLecturerEntity)

    @Query("SELECT count(*) FROM lecturers")
    suspend fun count(): Int

    @Query("SELECT * FROM lecturers")
    suspend fun get(): DBLecturerEntity
}