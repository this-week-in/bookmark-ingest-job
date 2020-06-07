package ttd.ingest

import org.springframework.batch.item.ItemWriter
import pinboard.Bookmark

open class BookmarkIngestItemWriter(private val jdbcBookmarkService: JdbcBookmarkService) : ItemWriter<Bookmark> {

  override fun write(items: List<Bookmark>) {
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