package asia.vmdigital.hermes.event.handler

import asia.vmdigital.hermes.event.Event
import asia.vmdigital.hermes.event.transport.UserContactUpdate
import asia.vmdigital.hermes.query.PlaceQuery
import asia.vmdigital.hermes.repository.UserRepository
import asia.vmdigital.hermes.service.FriendPickService
import asia.vmdigital.hermes.service.PlaceService
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ContactUpdateEventHandler(private val userRepository: UserRepository,
                                private val placeService: PlaceService,
                                private val friendPickService: FriendPickService) {

    private val logger: Logger = LoggerFactory.getLogger(ContactUpdateEventHandler::class.java)
    private val mapper = ObjectMapper()

    init {
        mapper.findAndRegisterModules()
    }

    @KafkaListener(topics = ["\${kafka.topic.userServiceContactUpdate}"])
    fun handleUserContactUpdate(transport: String) {
        logger.debug("Processing message from kafka {}", transport)
        try {
            val userContactUpdate = mapper.readValue(transport, UserContactUpdate::class.java)
            logger.debug("Got message from kafka {}", userContactUpdate)
            val event = userContactUpdate.event
            when(event) {
                Event.CONTACT_ADDED.eventName -> {
                    synchronizeUser(userContactUpdate.userToBeAdded!!)
                    handleContactAdded(userContactUpdate)
                }
                Event.CONTACT_REMOVED.eventName -> {
                    synchronizeUser(userContactUpdate.userToBeRemoved!!)
                    handleContactRemoved(userContactUpdate)
                }
                else -> logger.warn("Unrecognized event detected!!, event name {}", event)
            }
        } catch (e: JsonProcessingException) {
            logger.error("Error wile parsing json from kafka topic, message dropped", e)
        }
    }

    private fun synchronizeUser(userId: String) {
        userRepository.deleteUser(userId = userId).block()
        userRepository.getUser(userId = userId).block()
    }

    private fun handleContactAdded(userContactUpdate: UserContactUpdate) {
        logger.info("Handle user added contact event, user id {}", userContactUpdate.userId)
        val placesToPopulate = placeService.listPlaces(PlaceQuery(userId = userContactUpdate.userToBeAdded,
                size = Int.MAX_VALUE)).collectList().block()
        placesToPopulate!!.forEach({
            friendPickService.populateFriendPickForSpecificUser(it.userId!!, it!!,
                    userContactUpdate.userId!!)
        })
    }

    private fun handleContactRemoved(userContactUpdate: UserContactUpdate) {
        logger.info("Handle user removed contact event, user id {}", userContactUpdate.userId)
        val placesToPopulate = placeService.listPlaces(PlaceQuery(userId = userContactUpdate.userToBeRemoved,
                size = Int.MAX_VALUE)).collectList().block()
        placesToPopulate!!.forEach({
            friendPickService.unPopulateFriendPick(it.userId!!, it.placeId!!)
        })
    }
}