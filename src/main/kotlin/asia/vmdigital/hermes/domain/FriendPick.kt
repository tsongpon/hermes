package asia.vmdigital.hermes.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class FriendPick(@Id
                      var id: String? = null,
                      @Indexed
                      var userId: String? = null,
                      var placeId: String? = null,
                      val pickers: List<Picker> = ArrayList(),
                      var createTime: LocalDateTime? = null,
                      var updateTime: LocalDateTime? = null)