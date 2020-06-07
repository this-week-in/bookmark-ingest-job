package ttd.ingest

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableBatchProcessing
@SpringBootApplication
@EnableConfigurationProperties(IngestProperties::class)
class Main

fun main(args: Array<String>) {
  runApplication<Main>(*args)
}

