package noorg.kloud.vthelper.api.models.moodle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias ListCoursesResponse = List<ListCoursesResponseRootElem>

@Serializable
data class ListCoursesResponseRootElem(
    val error: Boolean,
    val data: ListCoursesResponseData,
)

@Serializable
data class ListCoursesResponseData(
    val courses: List<ListCoursesResponseCourse>,
    @SerialName("nextoffset")
    val nextOffset: Long,
)

@Serializable
data class ListCoursesResponseCourse(
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
    val courseImage: String,
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