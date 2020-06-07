package ttd.ingest

import org.apache.commons.logging.LogFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.util.*

@Service
@Transactional
class JdbcBookmarkService(private val jdbcTemplate: JdbcTemplate) {

  private val log = LogFactory.getLog(javaClass)

  fun createOrUpdate(extended: String, description: String, hash: String, href: String, meta: String, time: Date, tags: Array<String>) {

    try {
      val sqlForEachRow =
          """ 
        insert into BOOKMARK ( extended, description, hash, href, meta  ,  time , tags ) 
        values ( ?, ?, ?, ?, ?, ?, ? ) 
        on conflict  (hash)
        do update
         set
          extended = ?,
          description = ?,
          hash = ?,
          href = ?,
          meta = ?,
          time  = ?,
          tags = ?   
      """

      jdbcTemplate.update(sqlForEachRow) { preparedStatement ->

        val array: java.sql.Array = preparedStatement.connection.createArrayOf("varchar", tags)
        val timestamp = Timestamp.from(time.toInstant())

        preparedStatement.setString(1, extended)
        preparedStatement.setString(2, description)
        preparedStatement.setString(3, hash)
        preparedStatement.setString(4, href)
        preparedStatement.setString(5, meta)
        preparedStatement.setTimestamp(6, timestamp)
        preparedStatement.setArray(7, array)
        preparedStatement.setString(8, extended)
        preparedStatement.setString(9, description)
        preparedStatement.setString(10, hash)
        preparedStatement.setString(11, href)
        preparedStatement.setString(12, meta)
        preparedStatement.setTimestamp(13, timestamp)
        preparedStatement.setArray(14, array)
        preparedStatement.execute()
      }
    } catch (e: Throwable) {
      log.error(e)
    }
  }

  fun countBookmarksSince(since: Date): Int {
    val results = jdbcTemplate.queryForObject("select count(*) as the_count from BOOKMARK b where b.time > ?", since) { rs, _ ->
      rs.getInt("the_count")
    }
    return results
  }

}