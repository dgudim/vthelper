package noorg.kloud.vthelper.data.provider_models

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

data class ProvidedMoodleCourseEntity (
    val moodleId: Long,
    val courseModCode: String,

    // Set by the provider
    val isFromCurrentSemester: Boolean = false,

    val title: String,
    val description: String,

    val color: Color,

    val viewUrl: String,

    val coverImagePath: String
)