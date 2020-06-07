package ttd.ingest

import org.apache.commons.logging.LogFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pinboard.Bookmark
import pinboard.PinboardClient
import javax.sql.DataSource

@Configuration
class Step2Configuration(
    private val ingestProperties: IngestProperties,
    private val dataSource: DataSource,
    private val pinboardClient: PinboardClient,
    private val stepBuilderFactory: StepBuilderFactory) {

  private val log = LogFactory.getLog(javaClass)

  @Bean
  fun step2() =
      stepBuilderFactory
          .get("mark-as-ingested")
          .chunk<DbBookmark, DbBookmark>(100)
          .reader(unsynchronizedBookmarkItemReader())
          .processor(markPinboardAsSynchronizedItemProcessor())
          .writer(markAsReadItemWriter())
          .build()

  @Bean
  fun unsynchronizedBookmarkItemReader(): ItemReader<DbBookmark> {
    return JdbcCursorItemReaderBuilder<DbBookmark>()
        .dataSource(dataSource)
        .name("read-unsynchronized-bookmarks")
        .rowMapper(DbBookmarkRowMapper())
        .sql("select * from BOOKMARK where INGEST_SYNCHRONIZED = false")
        .build()
  }

  @Bean
  fun markPinboardAsSynchronizedItemProcessor(): ItemProcessor<DbBookmark, DbBookmark> {
    val ingestedTag = ingestProperties.ingestedTag
    return ItemProcessor<DbBookmark, DbBookmark> { bookmark ->
      val posts: Array<Bookmark> = this.pinboardClient.getPosts(url = bookmark.href).posts
      log.info("there are ${posts.size} posts for ${bookmark.href} in Pinboard.")
      posts.forEach {
        val newTags = mutableListOf<String>()
        newTags.addAll(it.tags)
        if (!newTags.contains(ingestedTag)) {
          newTags.add(ingestedTag)
          log.info("adding an ingest marker tag (${ingestedTag}) to ${it.href} in Pinboard.")
          pinboardClient.updatePost(it.href!!,
              it.description!!,
              it.extended!!,
              newTags.toTypedArray(),
              it.time!!,
              it.shared,
              it.toread
          )
        }
      }
      bookmark
    }
  }

  @Bean
  fun markAsReadItemWriter(): ItemWriter<DbBookmark> {
    return JdbcBatchItemWriterBuilder<DbBookmark>()
        .dataSource(this.dataSource)
        .sql("update BOOKMARK set ingest_synchronized = true where href = ?")
        .itemPreparedStatementSetter { dbBookmark, preparedStatement ->
          preparedStatement.setString(1, dbBookmark.href)
        }
        .build()
  }
}
