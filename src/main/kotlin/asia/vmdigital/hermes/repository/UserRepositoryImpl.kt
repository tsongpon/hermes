package asia.vmdigital.hermes.repository

import asia.vmdigital.hermes.component.AppProperties
import asia.vmdigital.hermes.domain.User
import asia.vmdigital.hermes.domain.UserContact
import asia.vmdigital.hermes.transport.ContactsTransport
import asia.vmdigital.hermes.transport.UserTransport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Repository
class UserRepositoryImpl(private val mongo: ReactiveMongoTemplate,
                         private val appProp: AppProperties): UserRepository {

    private val logger: Logger = LoggerFactory.getLogger(UserRepositoryImpl::class.java)

    override fun getUser(userId: String): Mono<User> {
        logger.debug("Getting user id {}", userId)
        return mongo.findOne(Query.query(Criteria.where("_id").`is`(userId)), User::class.java)
                .switchIfEmpty(getUserFromRemote(userId))
    }

    override fun save(user: User): Mono<User> {
        logger.debug("Saving user {}", user)
        return mongo.save(user)
    }

    override fun getUserFromRemote(userId: String): Mono<User> {
        logger.debug("Getting user from remote")
        val userServiceApi = appProp.userServiceUrl
        val userServiceApiKey = appProp.userServiceAPIKey
        val authorizationValue = "Bearer $userServiceApiKey"

        val webClient = WebClient.builder().baseUrl(userServiceApi!!)
                .defaultHeader("Authorization", authorizationValue)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()

        logger.debug("Getting user data id {}", userId)
        val userFromRemote = webClient.get().uri("/accounts/v1/users/$userId")
                .retrieve().bodyToMono(UserTransport::class.java)

        logger.debug("Getting user contact user id {}", userId)
        val contactsFromRemote = webClient.get().uri("/accounts/v1/users/$userId/contacts")
                .retrieve().bodyToMono(ContactsTransport::class.java)

        val userMono = Mono.zip(userFromRemote, contactsFromRemote).map { populateToUser(it.t1, it.t2) }
        userMono.subscribe {it ->
            save(it).block()
        }

        return userMono
    }

    private fun populateToUser(userTransport: UserTransport, contacts: ContactsTransport): User {
        val user = mapUserField(userTransport)
        val contactsList: ArrayList<UserContact> = ArrayList()
        contacts.contacts.forEach({it ->
            val contact = UserContact()
            contact.id = it.id
            contact.profileName = it.profileName
            contact.profilePhoto = it.profilePhoto
            contactsList.add(contact)
            user.contacts = contactsList
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
