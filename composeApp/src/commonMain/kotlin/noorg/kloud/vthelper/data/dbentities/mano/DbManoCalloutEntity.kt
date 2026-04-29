package noorg.kloud.vthelper.data.dbentities.mano

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "mano_callouts"
)
data class DbManoCalloutEntity (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "absolute_seq")
    val id: Int = 0,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "contents")
    val contents: String
)