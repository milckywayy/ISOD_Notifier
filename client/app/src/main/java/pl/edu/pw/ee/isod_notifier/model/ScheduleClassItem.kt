package pl.edu.pw.ee.isod_notifier.model

data class ScheduleClassItem (
    val name: String,
    val startTime: String,
    val endTime: String,
    val courseId: String,
    val typeOfClasses: String,
    val building: String,
    val room: String,
    val note: String,
    val isActive: Boolean
)