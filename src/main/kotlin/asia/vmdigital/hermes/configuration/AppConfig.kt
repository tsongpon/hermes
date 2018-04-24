package asia.vmdigital.hermes.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.scheduling.annotation.EnableAsync

@Configuration
@EnableReactiveMongoRepositories
@EnableAsync
open class AppConfig {
}