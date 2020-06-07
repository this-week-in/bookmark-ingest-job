package ttd.ingest

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object DateUtils {

  fun buildLocalDateTimeFrom(d: Date): LocalDateTime = LocalDateTime.ofInstant(Date(d.time).toInstant(), ZoneId.systemDefault())

}