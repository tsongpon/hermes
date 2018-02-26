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
import java.time.LocalDateTime

@Service
class PlaceService @Autowired constructor(private val repository: PlaceRepository,
                                          private val friendPickService: FriendPickService) {

    private val logger: Logger = LoggerFactory.getLogger(PlaceService::class.java)

    fun savePlace(placeToSave: Place): Mono<Place> {
        logger.debug("Saving place for userId {}", placeToSave.userId)
        return repository.getPlaceBy(placeToSave.userId!!, placeToSave.placeId!!)
                .defaultIfEmpty(placeToSave)
                .map { merge(it, placeToSave) }
                .flatMap { repository.savePlace(it) }
                .map { populateFriendPick(it, it.updateTime!!, it.type!!) }
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

    fun deletePlace(userId: String, id: String): Mono<Boolean> {
        return repository.getPlace(id).flatMap {
            friendPickService.unPopulateFriendPick(it.userId!!, it.placeId!!)
            repository.deletePlace(it.id!!)
        }
    }

    private fun merge(placeFromDb: Place, toBeMerge: Place): Place {
        logger.debug("Merging")
        toBeMerge.id = placeFromDb.id
        toBeMerge.createTime = placeFromDb.createTime

        return toBeMerge
    }

    private fun populateFriendPick(place: Place, time: LocalDateTime, type: String): Place {
        friendPickService.populateFriendPick(pickerUserId = place.userId!!,
                place = place, pickTime = time, source = type)
        logger.debug("Populating friendpick")
        return place
    }
}