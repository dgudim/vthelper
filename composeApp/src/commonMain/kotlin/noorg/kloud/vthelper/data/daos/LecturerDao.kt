package noorg.kloud.vthelper.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import noorg.kloud.vthelper.data.entities.LecturerEntity

@Dao
interface LecturerDao {
    @Insert
    suspend fun insert(item: LecturerEntity)

    @Query("SELECT count(*) FROM lecturers")
    suspend fun count(): Int

    @Query("SELECT * FROM lecturers")
    suspend fun get(): LecturerEntity
}