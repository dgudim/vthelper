package noorg.kloud.vthelper.data.dbentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "logged_in_user"
)
data class DBLoggedInUserEntity (
    @PrimaryKey
    @ColumnInfo(name = "student_id")
    val studentId: String,
    @ColumnInfo(name = "moodle_id")
    val moodleId: String,
    @ColumnInfo(name = "password")
    val password: String,
    @ColumnInfo(name = "session_valid")
    val isSessionValid: Boolean,
    @ColumnInfo(name = "personal_email")
    val personalEmail: String?,
    @ColumnInfo(name = "university_email")
    val universityEmail: String?,
    @ColumnInfo(name = "phone")
    val phone: String?,
    @ColumnInfo(name = "address")
    val address: String?,
    @ColumnInfo(name = "birth_date")
    val birthDate: String?,
    @ColumnInfo(name = "full_name")
    val fullName: String?,
    @ColumnInfo(name = "avatar_path")
    val avatarPath: String?,
    @ColumnInfo(name = "cookies_json")
    val cookiesJson: String,
)
