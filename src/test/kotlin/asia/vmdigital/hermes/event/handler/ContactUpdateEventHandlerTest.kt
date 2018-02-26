package asia.vmdigital.hermes.event.handler

import asia.vmdigital.hermes.domain.Follower
import asia.vmdigital.hermes.domain.Place
import asia.vmdigital.hermes.domain.User
import asia.vmdigital.hermes.query.PlaceQuery
import asia.vmdigital.hermes.repository.UserRepositoryImpl
import asia.vmdigital.hermes.service.FriendPickService
import asia.vmdigital.hermes.service.PlaceService
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class ContactUpdateEventHandlerTest {

    val mockUserRepository = mock(UserRepositoryImpl::class.java)
    val mockPlaceService = mock(PlaceService::class.java)
    val mockFriendPickService = mock(FriendPickService::class.java)

    val contactUpdateEventHandler = ContactUpdateEventHandler(mockUserRepository, mockPlaceService, mockFriendPickService)

    @Test
    fun handleUserConnected() {
        val returnedUser = User(id = "1300", followers = arrayListOf(
                Follower("fl-1", "profile-1", "photo1")))

        `when`(mockUserRepository.deleteUser("1300")).thenReturn(Mono.just(true))
        `when`(mockUserRepository.getUser("1300")).thenReturn(Mono.just(returnedUser))

        val yesterday = LocalDateTime.now().minusDays(1)
        val returnedPlaces = Place(userId = "1212312121", placeId = "place-1", updateTime = yesterday)

        val placeQuery = PlaceQuery(userId = "1212312121", size = Int.MAX_VALUE)
        `when`(mockPlaceService.listPlaces(placeQuery))
                .thenReturn(Flux.just(returnedPlaces))
        doNothing().`when`(mockFriendPickService).populateFriendPickForSpecificUser(
                "1212312121", returnedPlaces, "1300")

        val eventPayload = """
            {"time":"2018-02-26T15:37:18.856","event":"contactAdded","user_id":"1300","user_to_be_removed":null,"user_to_be_added":"1212312121"}
            """
        contactUpdateEventHandler.handleUserContactUpdate(eventPayload)

        verify(mockUserRepository, times(1)).deleteUser("1300")
        verify(mockUserRepository, times(1)).getUser("1300")
        verify(mockPlaceService, times(1)).listPlaces(placeQuery)
        verify(mockFriendPickService, times(1)).populateFriendPickForSpecificUser(
                "1212312121", returnedPlaces, "1300")
    }

    @Test
    fun handleUserDisConnected() {
        val returnedUser = User(id = "1300", followers = arrayListOf(
                Follower("fl-1", "profile-1", "photo1")))

        `when`(mockUserRepository.deleteUser("1300")).thenReturn(Mono.just(true))
        `when`(mockUserRepository.getUser("1300")).thenReturn(Mono.just(returnedUser))

        val yesterday = LocalDateTime.now().minusDays(1)
        val returnedPlaces = Place(userId = "1212312121", placeId = "place-1", updateTime = yesterday)

        val placeQuery = PlaceQuery(userId = "1212312121", size = Int.MAX_VALUE)
        `when`(mockPlaceService.listPlaces(placeQuery))
                .thenReturn(Flux.just(returnedPlaces))
        doNothing().`when`(mockFriendPickService).unPopulateFriendPick(
                "1212312121","place-1")

        val eventPayload = """
            {"time":"2018-02-26T15:37:18.856","event":"contactRemoved","user_id":"1300","user_to_be_removed":1212312121,"user_to_be_added":null}
            """
        contactUpdateEventHandler.handleUserContactUpdate(eventPayload)

        verify(mockUserRepository, times(1)).deleteUser("1300")
        verify(mockUserRepository, times(1)).getUser("1300")
        verify(mockPlaceService, times(1)).listPlaces(placeQuery)
        verify(mockFriendPickService, times(1)).unPopulateFriendPick(
                "1212312121", "place-1")
    }
}