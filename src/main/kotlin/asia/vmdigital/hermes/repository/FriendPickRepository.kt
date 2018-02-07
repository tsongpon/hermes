package asia.vmdigital.hermes.repository

import asia.vmdigital.hermes.domain.FriendPick
import reactor.core.publisher.Mono

interface FriendPickRepository {

    fun saveFriendPick(friendPick: FriendPick): Mono<FriendPick>

    fun getFriendPickBy(userId: String): Mono<FriendPick>
}