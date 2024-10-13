package pl.edu.pw.ee.isod_notifier.model

data class CourseDetailsItem (
    var finalGrade: String,
    var totalPoints: Double,
    var teachers: List<String>,
    var place: String
)