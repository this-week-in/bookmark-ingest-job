package ttd.ingest

import org.apache.commons.logging.LogFactory
import org.springframework.batch.item.ItemWriter
import pinboard.Bookmark

open class BookmarkIngestItemWriter(private val jdbcBookmarkService: JdbcBookmarkService) : ItemWriter<Bookmark> {

  private val log = LogFactory.getLog(javaClass)

  override fun write(items: List<Bookmark>) {
    log.info("trying to write ${items.size} bookmarks to the DB.")
    items.forEach {
      val date = it.time!!
      this.jdbcBookmarkService.createOrUpdate(
          it.extended!!,
          it.description!!,
          it.hash!!,
          it.href!!,
          it.meta!!,
          date,
          it.tags
      )
    }
  }

}