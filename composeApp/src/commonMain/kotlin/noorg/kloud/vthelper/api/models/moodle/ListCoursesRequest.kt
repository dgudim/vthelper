package noorg.kloud.vthelper.api.models.moodle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias ListCoursesRequest = List<ListCoursesRequestRootElem>

@Serializable
data class ListCoursesRequestRootElem(
    val index: Long,
    @SerialName("methodname")
    val methodName: String,
    val args: ListCoursesRequestArgs,
)

@Serializable
data class ListCoursesRequestArgs(
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
