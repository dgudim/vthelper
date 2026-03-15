package noorg.kloud.vthelper.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "logged_in_user"
)
data class LoggedInUserEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "password")
    val password: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "full_name")
    val fullName: String,
    @ColumnInfo(name = "avatar_path")
    val avatarPath: String,
    @ColumnInfo(name = "cookies_json")
    val cookiesJson: String,
)
