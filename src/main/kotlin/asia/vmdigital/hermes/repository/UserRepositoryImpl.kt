package asia.vmdigital.hermes.repository

import asia.vmdigital.hermes.domain.Follower
import asia.vmdigital.hermes.domain.User
import asia.vmdigital.hermes.transport.ContactsTransport
import asia.vmdigital.hermes.transport.UserTransport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Repository
class UserRepositoryImpl(private val mongo: ReactiveMongoTemplate): UserRepository {

    private val logger: Logger = LoggerFactory.getLogger(UserRepositoryImpl::class.java)

    @Value("\${hermes.userServiceUrl}")
    private var userServiceUrl = ""

    @Value("\${hermes.userServiceAPIKey}")
    private var userServiceAPIKey = ""

    override fun getUser(userId: String): Mono<User> {
        logger.debug("Getting user id {}", userId)
        return mongo.findOne(Query.query(Criteria.where("_id").`is`(userId)), User::class.java)
                .switchIfEmpty(getUserFromRemote(userId))
    }

    override fun deleteUser(userId: String): Mono<Boolean> {
        logger.debug("deleting user {}", userId)
        return mongo.remove(Query.query(Criteria.where("_id").`is`(userId)), User::class.java)
                .map { it.wasAcknowledged() }
    }

    private fun save(user: User): Mono<User> {
        logger.debug("Saving user {}", user)
        return mongo.save(user)
    }

    private fun getUserFromRemote(userId: String): Mono<User> {
        val userServiceApi = userServiceUrl
        val userServiceApiKey = userServiceAPIKey
        val authorizationValue = "Bearer $userServiceApiKey"

        val webClient = WebClient.builder().baseUrl(userServiceApi)
                .defaultHeader("Authorization", authorizationValue)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()

        logger.debug("Getting user data id {}", userId)
        val userFromRemote = webClient.get().uri("/accounts/v1/users/$userId")
                .retrieve().bodyToMono(UserTransport::class.java).log("Getting user $userId from remote")

        logger.debug("Getting user contact user id {}", userId)
        val followersFromRemote = webClient.get().uri("/accounts/v1/users/$userId/contactowners")
                .retrieve().bodyToMono(ContactsTransport::class.java)

        return Mono.zip(userFromRemote, followersFromRemote)
                .map { mapToUser(it.t1, it.t2) }
                .map { save(it) }.flatMap { it }
    }

    private fun mapToUser(userTransport: UserTransport, contacts: ContactsTransport): User {
        val user = mapUserField(userTransport)
        val followerList: ArrayList<Follower> = ArrayList()
        contacts.contacts.forEach({it ->
            val contact = Follower()
            contact.id = it.id
            contact.profileName = it.profileName
            contact.profilePhoto = it.profilePhoto
            followerList.add(contact)
            user.followers = followerList
        })

        return user
    }

    private fun mapUserField(transport: UserTransport): User {
        val user = User()
        user.id = transport.id
        user.profileName = transport.profileName
        user.profilePhoto = transport.profilePhoto

        return user
    }
}
