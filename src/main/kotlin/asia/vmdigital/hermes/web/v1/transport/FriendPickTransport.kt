package asia.vmdigital.hermes.web.v1.transport

import java.util.*
import kotlin.collections.ArrayList

data class FriendPickTransport(
        var id: String? = null,
        var userId: String? = null,
        var savedId: String? = null,
        var type: String? = null,
        var categories: ArrayList<String> = ArrayList(),
        var saveTime: Date? = null,
        var createTime: Date? = null,
        var friends: List<PickerTransport> = ArrayList(),
        var lon: Double? = null,
        var lat: Double? = null
)