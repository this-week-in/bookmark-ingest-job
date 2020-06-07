package ttd.ingest

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.LocalDateTime

class DbBookmarkRowMapper : RowMapper<DbBookmark> {

  override fun mapRow(rs: ResultSet, rowNum: Int): DbBookmark? =
      DbBookmark(
          rs.getInt("bookmark_id"),
          rs.getString("extended"),
          rs.getString("description"),
          rs.getString("meta"),
          rs.getString("hash"),
          rs.getString("href"),
          localDateTimeFromNullableDate(rs.getDate("time"))!!,
          rs.getArray("tags").array as Array<String>,
          rs.getString("publish_key"),
          localDateTimeFromNullableDate(rs.getDate("edited")),
          rs.getBoolean("deleted")
      )

  private fun localDateTimeFromNullableDate(d: java.sql.Date?): LocalDateTime? {
    return if (d != null) DateUtils.buildLocalDateTimeFrom(d) else null
  }
}
