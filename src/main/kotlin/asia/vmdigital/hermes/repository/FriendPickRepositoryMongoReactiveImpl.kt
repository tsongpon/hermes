package asia.vmdigital.hermes.repository

import asia.vmdigital.hermes.domain.FriendPick
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
class FriendPickRepositoryMongoReactiveImpl(private var mongoTemplate: ReactiveMongoTemplate): FriendPickRepository {

    val logger: Logger = LoggerFactory.getLogger(FriendPickRepositoryMongoReactiveImpl::class.java)

    override fun saveFriendPick(friendPick: FriendPick): Mono<FriendPick> {
        val now = LocalDateTime.now()
        if (friendPick.id == null) {
            friendPick.id = KeyGenerator.getKey()
            friendPick.createTime = now
            logger.debug("Creating new friendpick with generated key {}", friendPick.id)
        }
        friendPick.updateTime = now
        logger.debug("Saving friendPick id {}", friendPick.id)

        return mongoTemplate.save(friendPick)
    }

    override fun getFriendPickBy(userId: String): Mono<FriendPick> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}