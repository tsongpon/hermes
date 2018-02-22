package asia.vmdigital.hermes.web.v1.transport

import java.util.*

data class FriendPickTransport(
        var id: String? = null,
        var userId: String? = null,
        var placeId: String? = null,
        var type: String? = null,
        var saveTime: Date? = null,
        var createTime: Date? = null,
        var friends: List<PickerTransport> = ArrayList()
)