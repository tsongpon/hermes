package asia.vmdigital.hermes.web.v1.mapper

import asia.vmdigital.hermes.domain.FriendPick
import asia.vmdigital.hermes.domain.Picker
import asia.vmdigital.hermes.util.Utils
import asia.vmdigital.hermes.web.v1.transport.FriendPickTransport
import asia.vmdigital.hermes.web.v1.transport.PickerTransport

class FriendPickTransportMapper {
    companion object {
        fun map(friendPick: FriendPick): FriendPickTransport {
            val transport = FriendPickTransport()
            transport.id = friendPick.id
            transport.userId = friendPick.userId
            transport.placeId = friendPick.placeId
            transport.type = friendPick.source
            transport.categories = friendPick.categories
            transport.createTime = Utils.localDateTimeToDate(friendPick.createTime)
            transport.saveTime = Utils.localDateTimeToDate(friendPick.updateTime)
            transport.friends = friendPick.pickers.map { map(it) }
            if (friendPick.location != null) {
                transport.lon = friendPick.location!!.x
                transport.lat = friendPick.location!!.y
            }

            transport.friends = transport.friends.sortedWith(compareByDescending{it.pickTime})
            return transport
        }

        private fun map(picker: Picker): PickerTransport {
            val transport = PickerTransport()
            transport.friendId = picker.userId
            transport.profileName = picker.profileName
            transport.profilePhoto = picker.profilePhoto
            transport.pickTime = Utils.localDateTimeToDate(picker.pickTime)

            return transport
        }
    }
}