package asia.vmdigital.hermes.service

import asia.vmdigital.hermes.domain.Place
import asia.vmdigital.hermes.query.PlaceQuery
import asia.vmdigital.hermes.repository.PlaceRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PlaceService @Autowired constructor(private val repository: PlaceRepository) {

    private val logger: Logger = LoggerFactory.getLogger(PlaceService::class.java)

    fun savePlace(place: Place): Mono<Place> {
        logger.debug("Saving place for userId {}", place.userId)
        val placeFromDb = repository.getPlaceBy(place.userId!!, place.placeId!!)
        return placeFromDb.map({ it ->
            merge(it, place)
        }).flatMap({ repository.savePlace(it) }).switchIfEmpty(repository.savePlace(place))
    }

    fun listPlaces(query: PlaceQuery): Flux<Place> {
        logger.debug("Listing places")
        return repository.queryPlaces(query)
    }

    fun getPlace(id: String): Mono<Place> {
        logger.debug("Getting place id {}", id)
        return repository.getPlace(id)
    }

    fun countPlace(query: PlaceQuery): Mono<Long> {
        logger.debug("Counting places")
        return repository.countPlaces(query)
    }

    fun deletePlace(id: String): Mono<Boolean> {
        return repository.deletePlace(id)
    }

    private fun merge(placeFromDb: Place, toBeMerge: Place): Place {
        toBeMerge.id = placeFromDb.id
        toBeMerge.createTime = placeFromDb.createTime

        return toBeMerge
    }
}