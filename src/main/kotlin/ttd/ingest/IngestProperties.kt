package ttd.ingest

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("ingest")
data class IngestProperties(
    val ingestedTag: String,
    val startDate: String,  // how far back should we go if there's nothing in the DB?
    val refreshDays: Long, // how far back should we go if the database has already been initialized?
    val tags: Array<String>)

