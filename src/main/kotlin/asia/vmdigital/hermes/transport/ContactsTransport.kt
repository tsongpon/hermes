package asia.vmdigital.hermes.transport

import com.fasterxml.jackson.annotation.JsonProperty

class ContactsTransport {
    @JsonProperty("result")
    var contacts: List<Contact> = ArrayList()
}