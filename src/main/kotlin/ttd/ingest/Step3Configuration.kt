package ttd.ingest

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.Instant

@Configuration
class Step3Configuration(
    private val jdbcTemplate: JdbcTemplate,
    private val stepBuilderFactory: StepBuilderFactory) {

  @Bean
  fun step3() = stepBuilderFactory
      .get("update-records-worthy-of-review")
      .tasklet { _, _ ->


        jdbcTemplate.execute("update BOOKMARK set edited = ?") { ps ->
          ps.setTimestamp(1, Timestamp.from(Instant.now()))
          ps.execute()
        }


        val sqlForDupes = """
          update BOOKMARK set edited = null where description in ( 
            select b.description from bookmark b where b.deleted = false group by b.description having count(*) > 1 
          )
          and deleted = false 
        """
        jdbcTemplate.execute(sqlForDupes)

        val sqlForHttpUrls = """          
          update BOOKMARK set edited = null where description in ( 
            select b.description from BOOKMARK b where b.description similar to '%\(\d+\).*?%'
          )
          and deleted = false 
        """
        jdbcTemplate.execute(sqlForHttpUrls)

        RepeatStatus.FINISHED
      }
      .build()

}