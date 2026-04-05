package noorg.kloud.vthelper.data.provider_models

import androidx.compose.ui.graphics.Color

data class ProvidedMoodleCourseEntity (
    val moodleId: Long,

    val title: String,
    val description: String,

    val color: Color,

    val coverImagePath: String
)