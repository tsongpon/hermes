package asia.vmdigital.hermes.transport

import com.fasterxml.jackson.annotation.JsonProperty

class Contact {
    var id: String? = null
    @JsonProperty("profile_name")
    var profileName: String? = null
    @JsonProperty("profile_photo")
    var profilePhoto: String? = null
}