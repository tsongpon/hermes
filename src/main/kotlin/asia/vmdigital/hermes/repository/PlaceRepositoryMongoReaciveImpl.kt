package asia.vmdigital.hermes.repository

import asia.vmdigital.hermes.domain.Place
import asia.vmdigital.hermes.query.PlaceQuery
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
class PlaceRepositoryMongoReaciveImpl(private var mongoTemplate: ReactiveMongoTemplate): PlaceRepository {

    private val logger: Logger = LoggerFactory.getLogger(PlaceRepositoryMongoReaciveImpl::class.java)

    override fun savePlace(place: Place): Mono<Place> {
        val now = LocalDateTime.now()
        if (place.id == null) {
            place.id = KeyGenerator.getKey()
            place.createTime = now
        }
        place.updateTime = now
        logger.debug("Saving place {}", place)
        return mongoTemplate.save(place)
    }

    override fun getPlaceBy(userId: String, placeId: String): Mono<Place> {
        val byUserIdAndPlaceId = Query.query(Criteria.where("userId").`is`(userId)
                .and("placeId").`is`(placeId))
        return mongoTemplate.findOne(byUserIdAndPlaceId, Place::class.java)
    }

    override fun getPlace(id: String): Mono<Place> {
        logger.debug("Getting place id {}", id)
        val byId = Query.query(Criteria.where("_id").`is`(id))
        return mongoTemplate.findOne(byId, Place::class.java)
    }

    override fun countPlaces(query: PlaceQuery): Mono<Long> {
        return mongoTemplate.count(composeCriteria(query), Place::class.java)
    }

    override fun queryPlaces(query: PlaceQuery): Flux<Place> {
        logger.debug("Listing places with query {}", query)
        return mongoTemplate.find(composeCriteria(query), Place::class.java)
    }

    override fun deletePlace(id: String): Mono<Boolean> {
        logger.debug("Deleting place id {}", id)
        val criteria = Query.query(Criteria.where("_id").`is`(id))
        return mongoTemplate.remove(criteria, Place::class.java).map { it.wasAcknowledged() }
    }

    private fun composeCriteria(query: PlaceQuery): Query {
        val criteria = Query()
        if (query.userId != null) {
            criteria.addCriteria(Criteria.where("userId").`is`(query.userId))
        }
        if (query.placeId != null) {
            criteria.addCriteria(Criteria.where("placeId").`is`(query.placeId))
        }
        criteria.skip(query.start.toLong()).limit(query.size)

        return criteria
    }

}