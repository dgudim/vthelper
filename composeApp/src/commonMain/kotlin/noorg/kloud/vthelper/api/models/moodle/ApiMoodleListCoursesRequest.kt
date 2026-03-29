package noorg.kloud.vthelper.api.models.moodle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias ApiMoodleListCoursesRequest = List<ApiMoodleListCoursesRequestRootElem>

@Serializable
data class ApiMoodleListCoursesRequestRootElem(
    val index: Long,
    @SerialName("methodname")
    val methodName: String,
    val args: ApiMoodleListCoursesRequestArgs,
)

@Serializable
data class ApiMoodleListCoursesRequestArgs(
    val offset: Long,
    val limit: Long,
    val classification: String,
    val sort: String,

    @SerialName("customfieldname")
    val customFieldName: String,
    @SerialName("customfieldvalue")
    val customFieldValue: String,
    @SerialName("requiredfields")
    val requiredFields: List<String>,
)
