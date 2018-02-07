package asia.vmdigital.hermes.component

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("hermes")
class AppProperties {
    var userServiceUrl: String? = ""
    var userServiceAPIKey: String? = ""
}