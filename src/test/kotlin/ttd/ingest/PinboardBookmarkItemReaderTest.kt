package ttd.ingest

import org.junit.Assert
import org.junit.Test
import org.springframework.retry.support.RetryTemplate
import org.springframework.web.client.RestTemplate
import pinboard.PinboardClient
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class PinboardBookmarkItemReaderTest {

  private val rt = RetryTemplate()
  private val pbc = PinboardClient(System.getenv("PINBOARD_TOKEN"), RestTemplate())
  private val start = Date(Instant.parse("2018-06-25T00:00:00.00Z").toEpochMilli())
  private val stop = Date(Instant.parse("2018-06-30T00:00:00.00Z").toEpochMilli())
  private val bookmarkItemReader = PinboardBookmarkItemReader(
      { true }, this.pbc, this.rt, this.start, this.stop, tags = arrayOf("trump"))

  @Test
  fun `we should be able to read an item`() {
    val counter = AtomicLong()
    // find a date near the max and pad it for timezones
    val later = Date(Instant.ofEpochMilli(stop.time).plus(2L, ChronoUnit.DAYS).toEpochMilli())
    while (true) {
      val next = this.bookmarkItemReader.read() ?: break
      counter.incrementAndGet()
      println(next.time)
      Assert.assertTrue(next.time!!.before(later))
    }
    Assert.assertTrue(counter.get() > 0)
  }
}