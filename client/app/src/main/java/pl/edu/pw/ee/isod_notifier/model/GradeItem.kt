package pl.edu.pw.ee.isod_notifier.model

data class GradeItem (
    val name: String,
    val value: String,
    val weight: Float,
    val accounted: Boolean,
    val valueNote: String,
)