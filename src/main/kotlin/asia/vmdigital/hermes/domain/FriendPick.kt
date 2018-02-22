package asia.vmdigital.hermes.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

@Document
data class FriendPick(@Id
                      var id: String? = null,
                      @Indexed
                      var userId: String? = null,
                      var placeId: String? = null,
                      var source: String? = null,
                      val pickers: LinkedList<Picker> = LinkedList(),
                      var createTime: LocalDateTime? = null,
                      var updateTime: LocalDateTime? = null) {

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is FriendPick)
            return false
        val isSamePicker = pickers == other.pickers
        return userId == other.userId && placeId == other.placeId && isSamePicker
    }

    override fun hashCode(): Int =
            userId!!.hashCode() * 31 + placeId!!.hashCode()
}