package ttd.ingest

import org.apache.commons.logging.LogFactory
import org.springframework.batch.item.ItemReader
import org.springframework.retry.support.RetryTemplate
import pinboard.Bookmark
import pinboard.PinboardClient
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.atomic.AtomicLong

open class PinboardBookmarkItemReader(
    private val filter: (Bookmark) -> Boolean,
    private val pinboardClient: PinboardClient,
    private val retryTemplate: RetryTemplate,
    private val from: Date,
    private val to: Date,
    private val tags: Array<String>
) : ItemReader<Bookmark?> {

  private val offset = AtomicLong(from.time)
  private val monitor = Object()
  private val buffer = ArrayDeque<Bookmark>()
  private val log = LogFactory.getLog(javaClass)

  class NoNewsException(message: String?) : RuntimeException(message)

  private fun loadBookmarksForPeriod(from: Date, to: Date, ending: Boolean): Collection<Bookmark> {
    val spanString = rangeString(from, to)

    log.info("attempting to fetch results for $spanString.")
    log.info("the retryTemplate is thusly configured: ${retryTemplate}.")
    return this.retryTemplate
        .execute<List<Bookmark>, NoNewsException>({

          try {
            log.info("attempting to fetch posts ${this.tags.joinToString(",")} from ${from} to ${to}")

            val results: List<Bookmark> = this.pinboardClient
                .getAllPosts(tag = this.tags, fromdt = from, todt = to)
                .asList()

            log.info("results size: ${results.size}. Is Ending? ${ending}. Results are empty? ${results.isEmpty()}")

            if (results.isEmpty() && !ending) {
              log.info ( "there are no new news results to add")
              val msg = "received NO results for the span ${spanString}."
              log.warn(msg)
              throw NoNewsException(msg)
            }
            val filtered = results.filter(this.filter)
            log.info("filtered size: ${filtered.size}")
            filtered
          }
          catch (e: Throwable) {
            log.error("couldn't fetch! Returning an empty list.", e)
            emptyList<Bookmark>()
          }
        }, {
          log.info("returning an empty list for ${spanString}.")
          emptyList()
        })
  }

  private fun replenish(arrayDeque: ArrayDeque<Bookmark>): ArrayDeque<Bookmark> {
    val overlapWindow = 2 * (60 * 1000) * 60 // (( 60s * 60m = 1h ) * 2) = 2h
    while (arrayDeque.isEmpty() && this.offset.get() < this.to.time) {
      val periodStart = this.offset.get()
      val periodStop = Instant
          .ofEpochMilli(periodStart)
          .plus(7, ChronoUnit.DAYS)
          .toEpochMilli()
      val begin = Date(periodStart)
      val end = Date(periodStop)
      val range = rangeString(begin, end)
      this.log.info("buffer is empty. replenishing with results from ${range}")
      val elements = loadBookmarksForPeriod(begin, end, end.time >= System.currentTimeMillis())
      arrayDeque.addAll(elements)
      this.log.info("added ${elements.size} bookmark(s) for the results from ${range}")
      this.offset.set(periodStop - overlapWindow)
      /*
          subtract an $overlapWindow 's time from the end date to purposefully add some
          overlap in case we've missed anything since we started processing
          in the case of brand new events. There's no real reason for a single run through this loop
          to ever take 2h, but.. WHO KNOWS?
       */
    }
    return arrayDeque
  }

  private fun rangeString(s1: Date, s2: Date) = " [ ${s1}-${s2} ] "

  override fun read(): Bookmark? {
    synchronized(this.monitor) {
      return replenish(this.buffer).poll()
    }
  }
}