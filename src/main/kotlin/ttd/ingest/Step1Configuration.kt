package ttd.ingest

import org.apache.commons.logging.LogFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import pinboard.Bookmark
import pinboard.PinboardClient
import java.time.Instant
import java.util.*

@Configuration
class Step1Configuration(
    private val pinboardClient: PinboardClient,
    private val stepBuilderFactory: StepBuilderFactory,
    private val ingestProperties: IngestProperties,
    private val bookmarkService: JdbcBookmarkService) {

  private val log = LogFactory.getLog(javaClass)

  @Bean
  fun retryTemplate() = RetryTemplate()
      .apply {
        setBackOffPolicy(ExponentialBackOffPolicy())
        setRetryPolicy(SimpleRetryPolicy(3))
      }

  @Bean
  @StepScope
  fun pinboardBookmarkItemReader(): PinboardBookmarkItemReader {
    val now = Instant.now()
    val nowDate = Date(now.toEpochMilli())
    val refreshDaysInMillis = this.ingestProperties.refreshDays * 60 * 60 * 24 * 1000
    val sinceInstant: Instant = Instant.now().minusMillis(refreshDaysInMillis)
    val result: Int = this.bookmarkService.countBookmarksSince(Date(sinceInstant.toEpochMilli()))
    val startDate: Date =
        if (result > 0) {
          Date(sinceInstant.toEpochMilli())
        } else {
          Date(Instant.parse(this.ingestProperties.startDate).toEpochMilli())
        }
    log.info("the start date will be $startDate")
    return PinboardBookmarkItemReader(
        { !it.tags.contains(this.ingestProperties.ingestedTag) },
        this.pinboardClient,
        this.retryTemplate(),
        startDate,
        nowDate,
        this.ingestProperties.tags
    )
  }

  @Bean
  fun ingestPinboardBookmarksItemWriter() = BookmarkIngestItemWriter(bookmarkService)

  @Bean
  fun step1() =
      stepBuilderFactory
          .get("pinboard-to-db-step")
          .chunk<Bookmark, Bookmark>(100)
          .reader(pinboardBookmarkItemReader())
          .writer(ingestPinboardBookmarksItemWriter())
          .build()


}

