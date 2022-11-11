package api

import org.slf4j.LoggerFactory
import org.springframework.boot.Banner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.boot.context.event.ApplicationFailedEvent
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener

@SpringBootApplication
class ApiLauncher {

    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(ApiLauncher::class.java)

        fun load(args: Array<String>) {

            SpringApplicationBuilder()
                .sources(ApiLauncher::class.java)
                .web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .listeners(
                    ApplicationListener { event: Any ->
                        when (event) {
                            is ApplicationEnvironmentPreparedEvent -> {
                                log.info("Iniciando API...")
                            }

                            is ApplicationReadyEvent -> {
                                log.info("La API ha sido inicializada")
                            }

                            is ApplicationFailedEvent -> {
                                log.error("Application failed", event.exception)
                            }
                        }
                    }
                )
                .run(*args)
        }
    }

}