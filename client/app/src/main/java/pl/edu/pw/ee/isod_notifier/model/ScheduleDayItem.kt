package pl.edu.pw.ee.isod_notifier.model;

data class ScheduleDayItem (
    val dayOfWeek: Int,
    val isDayOff: Boolean,
    val lessons: List<ScheduleClassItem>
)
