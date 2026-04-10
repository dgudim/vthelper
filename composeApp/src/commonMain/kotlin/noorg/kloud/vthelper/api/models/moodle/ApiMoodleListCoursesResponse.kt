package noorg.kloud.vthelper.api.models.moodle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias ApiMoodleListCoursesResponse = List<ApiMoodleListCoursesResponseRootElem>

@Serializable
data class ApiMoodleListCoursesResponseRootElem(
    val error: Boolean,
    val data: ApiMoodleListCoursesResponseData,
)

@Serializable
data class ApiMoodleListCoursesResponseData(
    val courses: List<ApiMoodleListCoursesResponseCourse>,
    @SerialName("nextoffset")
    val nextOffset: Long,
)

@Serializable
data class ApiMoodleListCoursesResponseCourse(
    val id: Long,
    @SerialName("fullname")
    val fullName: String,
    @SerialName("shortname")
    val shortName: String,
    @SerialName("idnumber")
    val idNumber: String,
    val summary: String,
    @SerialName("summaryformat")
    val summaryFormat: Long,
    @SerialName("startdate")
    val startDate: Long,
    @SerialName("enddate")
    val endDate: Long,
    @SerialName("visible")
    val isVisible: Boolean,
    @SerialName("showactivitydates")
    val showActivityDates: Boolean,
    @SerialName("showcompletionconditions")
    val showCompletionConditions: String?,
    @SerialName("pdfexportfont")
    val pdfExportFont: String,
    @SerialName("fullnamedisplay")
    val fullNameDisplay: String,
    @SerialName("viewurl")
    val viewUrl: String,
    @SerialName("courseimage")
    val courseImageBase64OrUrl: String,
    val progress: Long,
    @SerialName("hasprogress")
    val hasProgress: Boolean,
    @SerialName("isfavourite")
    val isFavourite: Boolean,
    @SerialName("hidden")
    val isHidden: Boolean,
    @SerialName("showshortname")
    val showShortName: Boolean,
    @SerialName("coursecategory")
    val courseCategory: String,
)