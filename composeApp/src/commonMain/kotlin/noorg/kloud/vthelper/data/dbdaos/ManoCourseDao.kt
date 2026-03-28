package noorg.kloud.vthelper.data.dbdaos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import noorg.kloud.vthelper.data.dbentities.ManoCourseEntity

@Dao
interface ManoCourseDao {
    @Insert
    suspend fun insert(item: ManoCourseEntity)

    @Query("SELECT count(*) FROM mano_courses")
    suspend fun count(): Int

    @Query("SELECT * FROM mano_courses")
    fun getAllAsFlow(): Flow<List<ManoCourseEntity>>
}