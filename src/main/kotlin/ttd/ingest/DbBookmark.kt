package ttd.ingest


import java.time.LocalDateTime

data class DbBookmark(
    val bookmarkId: Int,
    val extended: String,
    val description: String,
    val meta: String,
    val hash: String,
    val href: String,
    val time: LocalDateTime,
    val tags: Array<String>,
    val publishKey: String?,
    val edited: LocalDateTime?,
    val deleted: Boolean)