package asia.vmdigital.hermes.event.transport

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class UserContactUpdate(var event: String? = null,
                             @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
                             @JsonSerialize(using = LocalDateTimeSerializer::class)
                             @JsonDeserialize(using = LocalDateTimeDeserializer::class)
                             var time: LocalDateTime? = null,
                             var userId: String? = null,
                             var userToBeRemoved: String? = null,
                             var userToBeAdded: String? = null
)