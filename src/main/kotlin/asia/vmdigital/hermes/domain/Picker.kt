package asia.vmdigital.hermes.domain

import java.time.LocalDateTime

class Picker(var userId: String? = null,
             var profileName: String? = null,
             var profilePhoto: String? = null,
             var pickTime: LocalDateTime? = null)