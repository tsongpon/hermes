package asia.vmdigital.hermes.domain

import java.time.LocalDateTime

data class Picker(var userId: String? = null,
             var profileName: String? = null,
             var profilePhoto: String? = null,
             var pickTime: LocalDateTime? = null) {

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is Picker)
            return false
        return userId == other.userId
    }

    override fun hashCode(): Int =
            userId!!.hashCode() * 31
}