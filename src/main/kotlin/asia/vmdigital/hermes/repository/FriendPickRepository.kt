package asia.vmdigital.hermes.repository

import asia.vmdigital.hermes.domain.FriendPick
import asia.vmdigital.hermes.query.FriendPickQuery
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FriendPickRepository {

    fun saveFriendPick(friendPick: FriendPick): Mono<FriendPick>

    fun getFriendPickBy(userId: String, placeId: String): Mono<FriendPick>

    fun queryFriendPicks(query: FriendPickQuery): Flux<FriendPick>

    fun countFriendPick(query: FriendPickQuery): Mono<Long>

    fun deleteFriendPick(id: String): Mono<Boolean>

    fun getFriendPickByPlaceIdAndPickerId(placeId: String, pickerId: String): Flux<FriendPick>
}