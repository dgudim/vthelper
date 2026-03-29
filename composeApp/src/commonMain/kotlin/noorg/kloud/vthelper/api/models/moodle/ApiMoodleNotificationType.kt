package noorg.kloud.vthelper.api.models.moodle

enum class ApiMoodleNotificationType(key: String) {
    ASSIGNMENT("message_provider_mod_assign_assign_notification_enabled"),
    ASSIGNMENT_DUE_SOON("message_provider_mod_assign_assign_due_soon_enabled"),
    ASSIGNMENT_OVERDUE("message_provider_mod_assign_assign_overdue_enabled"),
    ASSIGNMENT_DUE_7_DAYS("message_provider_mod_assign_assign_due_digest_enabled"),
    ASSIGNMENT_FEEDBACK_NOTIFICATION("message_provider_mod_feedback_submission_enabled"),
    ASSIGNMENT_FEEDBACK_REMINDER("message_provider_mod_feedback_message_enabled"),

    FORUM_POSTS("message_provider_mod_forum_posts_enabled"),
    FORUM_DIGESTS("message_provider_mod_forum_digests_enabled"),
    REGISTRATIONS_QUEUE_MOVE_UP("message_provider_mod_grouptool_grouptool_moveupreg_enabled"),
    LESSON_ESSAY_GRADED("message_provider_mod_lesson_graded_essay_enabled"),
    QUESTIONNAIRE_REMINDER("message_provider_mod_questionnaire_message_enabled"),
    QUESTIONNAIRE_SUBMISSION("message_provider_mod_questionnaire_notification_enabled"),
    QUIZ_OPENS_SOON("message_provider_mod_quiz_quiz_open_soon_enabled"),

    SCHEDULER_INVITATION_TO_BOOK_SLOT("message_provider_mod_scheduler_invitation_enabled"),
    SCHEDULER_BOOKING_MADE_OR_CANCELLED("message_provider_mod_scheduler_bookingnotification_enabled"),
    SCHEDULER_UPCOMING_APPOINTMENT("message_provider_mod_scheduler_reminder_enabled"),

    TURNITIN_DIGITAL_RECEIPT("message_provider_mod_turnitintooltwo_submission_enabled"),
    TURNITIN_NON_SUBMITTER("message_provider_mod_turnitintooltwo_nonsubmitters_enabled"),
    TURNITIN_DIGITAL_RECEIPT_INSTRUCTOR("message_provider_mod_turnitintooltwo_notify_instructor_of_submission_enabled"),

    NEW_LOGIN("message_provider_moodle_newlogin_enabled"),
    COURSE_COMPLETED("message_provider_moodle_coursecompleted_enabled"),
    COURSE_CONTENT_CHANGED("message_provider_moodle_coursecontentupdated_enabled"),

    BADGE_RECEPIENT("message_provider_moodle_badgerecipientnotice_enabled"),
    BADGE_CREATOR("message_provider_moodle_badgecreatornotice_enabled")
}