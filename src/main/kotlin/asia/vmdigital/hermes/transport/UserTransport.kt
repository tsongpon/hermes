package asia.vmdigital.hermes.transport

import com.fasterxml.jackson.annotation.JsonProperty

class UserTransport {
    val id: String? = null
    @JsonProperty("profile_name")
    val profileName: String? = null
    @JsonProperty("profile_photo")
    val profilePhoto: String? = null
    val contacts: List<UserTransport> = ArrayList()
}