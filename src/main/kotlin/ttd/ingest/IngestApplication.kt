package ttd.ingest

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class IngestApplication(private val jobBuilderFactory: JobBuilderFactory) {

  @Bean
  fun job(step1: Step1Configuration, step2: Step2Configuration, step3: Step3Configuration) =
      jobBuilderFactory
          .get("ingest-news")
          .start(step1.step1())
          .next(step2.step2())
          .next(step3.step3())
          .incrementer(RunIdIncrementer())
          .build()

}
