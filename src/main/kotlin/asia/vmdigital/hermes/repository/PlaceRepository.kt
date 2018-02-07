package asia.vmdigital.hermes.repository

import asia.vmdigital.hermes.domain.Place
import asia.vmdigital.hermes.query.PlaceQuery
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PlaceRepository {

    fun savePlace(place : Place): Mono<Place>

    fun getPlace(id: String): Mono<Place>

    fun getPlaceBy(userId: String, placeId: String): Mono<Place>

    fun queryPlaces(query: PlaceQuery): Flux<Place>

    fun countPlaces(query: PlaceQuery): Mono<Long>

    fun deletePlace(id: String): Mono<Boolean>
}