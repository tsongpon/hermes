package asia.vmdigital.hermes.web.v1.mapper

import asia.vmdigital.hermes.util.Utils
import asia.vmdigital.hermes.domain.Place
import asia.vmdigital.hermes.web.v1.transport.PlaceTransport
import org.springframework.data.geo.Point

class PlaceTransportMapper {
    companion object {
        fun map(place: Place): PlaceTransport {
            val transport = PlaceTransport()
            transport.id = place.id
            transport.userId = place.userId
            transport.placeId = place.placeId
            transport.type = place.type
            transport.categories = place.categories
            transport.createAt = Utils.localDateTimeToDate(place.createTime)
            transport.updateAt = Utils.localDateTimeToDate(place.updateTime)
            if (place.location != null) {
                transport.lon = place.location!!.x
                transport.lat = place.location!!.y
            }

            return transport
        }

        fun map(transport: PlaceTransport): Place {
            val place = Place()
            place.id = transport.id
            place.userId = transport.userId
            place.placeId = transport.placeId
            place.type = transport.type
            place.categories = transport.categories
            place.createTime  = Utils.dateToLocalDateTime(transport.createAt)
            place.updateTime = Utils.dateToLocalDateTime(transport.updateAt)
            if (transport.lat != null && transport.lon != null) {
                place.location = Point(transport.lon!!, transport.lat!!)
            }

            return place
        }
    }
}