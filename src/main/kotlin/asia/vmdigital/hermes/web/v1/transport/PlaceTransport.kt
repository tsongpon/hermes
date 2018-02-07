package asia.vmdigital.hermes.web.v1.transport

import java.util.*

data class PlaceTransport(
    var id: String? = null,
    var userId: String? = null,
    var placeId: String? = null,
    var type: String? = null,
    var createAt: Date? = null,
    var updateAt: Date? = null,
    var lon: Double? = null,
    var lat: Double? = null
)