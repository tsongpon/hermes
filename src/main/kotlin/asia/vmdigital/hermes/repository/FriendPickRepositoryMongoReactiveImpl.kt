package asia.vmdigital.hermes.repository

import asia.vmdigital.hermes.domain.FriendPick
import asia.vmdigital.hermes.domain.Place
import asia.vmdigital.hermes.query.FriendPickQuery
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import org.springframework.data.mongodb.core.aggregation.MatchOperation



@Repository
class FriendPickRepositoryMongoReactiveImpl(private var mongoTemplate: ReactiveMongoTemplate): FriendPickRepository {

    private val logger: Logger = LoggerFactory.getLogger(FriendPickRepositoryMongoReactiveImpl::class.java)

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

    fun getFriendPickByIds(ids: List<String?>): List<FriendPick> {
        val byId = Query.query(Criteria.where("_id").`in`(ids))
        return mongoTemplate.find(byId, FriendPick::class.java).collectList().block()!!
    }

    fun getFriendPickById(id: String): Mono<FriendPick> {
        val byId = Query.query(Criteria.where("_id").`is`(id))
        return mongoTemplate.findOne(byId, FriendPick::class.java)
    }

    override fun getFriendPickBy(userId: String, placeId: String): Mono<FriendPick> {
        logger.debug("Getting friendpick of user {}, place : {}", userId, placeId)
        return mongoTemplate.findOne(Query.query(Criteria.where("userId").`is`(userId))
                .addCriteria(Criteria.where("placeId").`is`(placeId)), FriendPick::class.java)
    }

    override fun queryFriendPicks(query: FriendPickQuery): Flux<FriendPick> {
        logger.debug("Listing friendPick by userId {}", query.userId)
        return mongoTemplate.find(composeCriteria(query), FriendPick::class.java)
    }

    override fun countFriendPick(query: FriendPickQuery): Mono<Long> {
        return mongoTemplate.count(composeCriteria(query), FriendPick::class.java)
    }

    override fun deleteFriendPick(id: String): Mono<Boolean> {
        val criteria = Query.query(Criteria.where("_id").`is`(id))
        return mongoTemplate.remove(criteria, FriendPick::class.java).map { it.wasAcknowledged() }
    }

    override fun getFriendPickByPlaceIdAndPickerId(placeId: String, pickerId: String): Flux<FriendPick> {
        val unwind = Aggregation.unwind("pickers")
        val matchPlaceId = Aggregation.match(Criteria("placeId").`is`(placeId))
        val matchPicker = Aggregation.match(Criteria("pickers.userId").`is`(pickerId))
        val projection = Aggregation.project("id")

        val aggregation = Aggregation.newAggregation(matchPlaceId, unwind, matchPicker, projection)
        val matchedId = mongoTemplate.aggregate(aggregation, "friendPick", FriendPick::class.java)
                .map { it.id }

        return matchedId.flatMap { getFriendPickById(it!!) }
    }

    private fun composeCriteria(query: FriendPickQuery): Query {
        val criteria = Query()
        if (query.userId != null) {
            criteria.addCriteria(Criteria.where("userId").`is`(query.userId))
        }
        criteria.skip(query.start.toLong()).limit(query.size)

        return criteria
    }
}