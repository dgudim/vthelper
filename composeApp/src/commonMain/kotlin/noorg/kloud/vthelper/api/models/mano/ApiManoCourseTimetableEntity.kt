package noorg.kloud.vthelper.api.models.mano

import kotlin.time.Duration

enum class ManoTimetableWeekday(day: String) {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
}

enum class ApiManoTimetableEntityWeek(val w: String) {
    ALL("-"), FIRST("1"), SECOND("2")
}

enum class ApiManoTimetableEntityType(val s: String) {
    LECTURE("lecture"), PRACTICE("practice"), LAB("lab")
}

data class ApiManoCourseTimetableEntity(
    val weekDay: ManoTimetableWeekday,
    val week: ApiManoTimetableEntityWeek,
    val startTime: Duration,
    val endTime: Duration,
    val auditorium: String,
    val type: ApiManoTimetableEntityType,
    val lecturerFullName: String
)