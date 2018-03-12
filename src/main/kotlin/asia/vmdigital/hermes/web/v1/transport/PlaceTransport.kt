package asia.vmdigital.hermes.web.v1.transport

import java.util.*
import kotlin.collections.ArrayList

data class PlaceTransport(
    var id: String? = null,
    var userId: String? = null,
    var savedId: String? = null,
    var type: String? = null,
    var categories: ArrayList<String> = ArrayList(),
    var createAt: Date? = null,
    var updateAt: Date? = null,
    var lon: Double? = null,
    var lat: Double? = null
)