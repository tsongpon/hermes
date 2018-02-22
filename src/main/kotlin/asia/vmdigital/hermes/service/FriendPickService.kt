package asia.vmdigital.hermes.service

import asia.vmdigital.hermes.domain.Follower
import asia.vmdigital.hermes.domain.FriendPick
import asia.vmdigital.hermes.domain.Picker
import asia.vmdigital.hermes.domain.User
import asia.vmdigital.hermes.query.FriendPickQuery
import asia.vmdigital.hermes.repository.FriendPickRepository
import asia.vmdigital.hermes.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class FriendPickService(private val friendPickRepository: FriendPickRepository,
                        private val userRepository: UserRepository) {

    private val logger: Logger = LoggerFactory.getLogger(FriendPickService::class.java)

    @Async
    fun populateFriendPick(pickerUserId: String, placeId: String, pickTime: LocalDateTime, source: String) {
        logger.debug("Populating friendPick picker {}, placeId {}", pickerUserId, placeId)
        val pickerUser = userRepository.getUser(pickerUserId).block()
        val targetUsers = pickerUser!!.followers
        logger.debug("Get target user {}", targetUsers!!.size)
        targetUsers.forEach {
            val friendPickFromDb = friendPickRepository.getFriendPickBy(it.id!!, placeId).block()
            if (friendPickFromDb == null) {
                createNewFriendPick(pickerUser, placeId, it, pickTime, source)
            } else {
                logger.debug("Update existing friendPick")
                updateFriendPick(pickerUser, friendPickFromDb)
            }
        }
    }

    @Async
    fun unPopulateFriendPick(pickerId: String, placeId: String) {
        val friendPickToUpdate = friendPickRepository.getFriendPickByPlaceIdAndPickerId(placeId, pickerId)
                .collectList().block()!!
        friendPickToUpdate.forEach({
            val pickerToRemove = Picker(userId = pickerId)
            it.pickers.remove(pickerToRemove)
            if(it.pickers.size > 0) {
                friendPickRepository.saveFriendPick(it).block()
            } else {
                friendPickRepository.deleteFriendPick(it.id!!).block()
            }
        })
    }

    fun queryFriendPicks(query: FriendPickQuery): Flux<FriendPick> {
        logger.debug("Query friend pick, query {}", query)
        return friendPickRepository.queryFriendPicks(query)
    }

    fun count(query: FriendPickQuery): Mono<Long> {
        return friendPickRepository.countFriendPick(query)
    }

    fun delete(id: String): Mono<Boolean> {
        return friendPickRepository.deleteFriendPick(id)
    }

    fun getByPlaceIdAndPickerId(placeId: String, pickerId: String): Flux<FriendPick> {
        return friendPickRepository.getFriendPickByPlaceIdAndPickerId(placeId, pickerId)
    }

    private fun updateFriendPick(pickerUser: User, existingFriendPick: FriendPick) {
        val picker = Picker(userId = pickerUser.id,
                profileName = pickerUser.profileName,
                profilePhoto = pickerUser.profilePhoto,
                pickTime = LocalDateTime.now())
        existingFriendPick.pickers.remove(picker)
        existingFriendPick.pickers.add(picker)

        friendPickRepository.saveFriendPick(existingFriendPick).block()
    }

    private fun createNewFriendPick(pickerUser: User, placeId: String,
                                    follower: Follower, pickTime: LocalDateTime, source: String) {
        logger.debug("New friendPick")
        val newFriendPick = FriendPick()
        newFriendPick.userId = follower.id
        newFriendPick.placeId = placeId
        newFriendPick.source = source

        val picker = Picker(userId = pickerUser.id,
                profileName = pickerUser.profileName,
                profilePhoto = pickerUser.profilePhoto,
                pickTime = pickTime)
        newFriendPick.pickers.add(picker)

        friendPickRepository.saveFriendPick(newFriendPick).block()
    }
}