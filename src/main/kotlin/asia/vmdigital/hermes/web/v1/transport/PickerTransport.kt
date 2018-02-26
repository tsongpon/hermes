package asia.vmdigital.hermes.web.v1.transport

import java.util.*

data class PickerTransport(var friendId: String? = null,
                           var profileName: String? = null,
                           var profilePhoto: String? = null,
                           var pickTime: Date? = null)