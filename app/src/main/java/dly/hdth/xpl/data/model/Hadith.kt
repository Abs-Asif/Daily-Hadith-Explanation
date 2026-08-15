package dly.hdth.xpl.data.model

data class Hadith(
    val dateCode: String, // e.g. "160826"
    val arabic: String,
    val bangla: String,
    val explanationMarkdown: String,
    val isPlaceholder: Boolean = false
)
