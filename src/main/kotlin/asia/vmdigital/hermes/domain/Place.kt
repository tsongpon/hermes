package asia.vmdigital.hermes.domain

import org.springframework.data.annotation.Id
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class Place(@Id
                 var id: String? = null,
                 @Indexed
                 var userId: String? = null,
                 var placeId: String? = null,
                 var type: String? = null,
                 var categories: ArrayList<String> = ArrayList(),
                 var createTime: LocalDateTime? = null,
                 var updateTime: LocalDateTime? = null,
                 @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
                 var location: Point? = null)