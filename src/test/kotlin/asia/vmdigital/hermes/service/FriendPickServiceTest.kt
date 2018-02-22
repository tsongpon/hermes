package asia.vmdigital.hermes.service

import asia.vmdigital.hermes.domain.Follower
import asia.vmdigital.hermes.domain.FriendPick
import asia.vmdigital.hermes.domain.Picker
import asia.vmdigital.hermes.domain.User
import asia.vmdigital.hermes.repository.FriendPickRepositoryMongoReactiveImpl
import asia.vmdigital.hermes.repository.UserRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class FriendPickServiceTest {

    private val mockFriendPickRepository = mock(FriendPickRepositoryMongoReactiveImpl::class.java)
    private val mockUserRepository = mock(UserRepository::class.java)

    private val friendPickService = FriendPickService(mockFriendPickRepository, mockUserRepository)

    @Test
    fun populateNewFriendPick() {

        val mockUser = User(id = "user-1",
                profileName = "tum",
                profilePhoto = "tum-pic")
        mockUser.followers = arrayListOf(
                Follower("fl-1", "profile-1", "photo1"))

        val pickTime = LocalDateTime.now()

        val friendPickFl1 = FriendPick()
        friendPickFl1.userId = "fl-1"
        friendPickFl1.placeId = "place-1"
        val picker = Picker(userId = "user-1", profilePhoto = "tum",
                profileName = "tum-pic", pickTime = pickTime)
        friendPickFl1.pickers.add(picker)

        val savedFriendPick = FriendPick(
                id = "some-id",
                userId = "fl-1",
                placeId = "place-1",
                createTime = friendPickFl1.createTime,
                updateTime = friendPickFl1.updateTime
                )

        `when`(mockUserRepository.getUser("user-1")).thenReturn(Mono.just(mockUser))
        `when`(mockFriendPickRepository.getFriendPickBy("fl-1", "place-1")).thenReturn(Mono.empty())
        `when`(mockFriendPickRepository.saveFriendPick(friendPickFl1)).thenReturn(Mono.just(savedFriendPick))

        friendPickService.populateFriendPick("user-1", "place-1", pickTime, "Google")

        verify(mockUserRepository, times(1)).getUser("user-1")
        verify(mockFriendPickRepository, times(1)).saveFriendPick(friendPickFl1)
    }

    @Test
    fun populateExistingFriendPick() {

        val existingFriendPick = FriendPick()
        existingFriendPick.userId = "fl-1"
        existingFriendPick.placeId = "place-1"
        val existPicker = Picker(userId = "user-add-long-ago", profilePhoto = "songpon",
                profileName = "songpon-pic", pickTime = LocalDateTime.now().minusDays(1))
        existingFriendPick.pickers.add(existPicker)

        val mockUser = User(id = "user-1",
                profileName = "tum",
                profilePhoto = "tum-pic")
        mockUser.followers = arrayListOf(
                Follower("fl-1", "profile-1", "photo1"))

        val pickTime = LocalDateTime.now()

        val updatedFriendPick = FriendPick()
        updatedFriendPick.userId = "fl-1"
        updatedFriendPick.placeId = "place-1"
        val picker = Picker(userId = "user-1", profilePhoto = "tum",
                profileName = "tum-pic", pickTime = pickTime)
        updatedFriendPick.pickers.add(existPicker)
        updatedFriendPick.pickers.add(picker)

        val savedFriendPick = FriendPick(
                id = "some-id",
                userId = "fl-1",
                placeId = "place-1",
                createTime = updatedFriendPick.createTime,
                updateTime = updatedFriendPick.updateTime
        )

        `when`(mockUserRepository.getUser("user-1")).thenReturn(Mono.just(mockUser))
        `when`(mockFriendPickRepository.getFriendPickBy("fl-1", "place-1"))
                .thenReturn(Mono.just(existingFriendPick))
        `when`(mockFriendPickRepository.saveFriendPick(updatedFriendPick)).thenReturn(Mono.just(savedFriendPick))

        friendPickService.populateFriendPick("user-1", "place-1", pickTime, "Google")

        verify(mockUserRepository, times(1)).getUser("user-1")
        verify(mockFriendPickRepository, times(1)).saveFriendPick(updatedFriendPick)
    }

    @Test
    fun testUnPopulateFriendPickWith1Picker() {
        val existingFriendPick = FriendPick()
        existingFriendPick.id = "id-1"
        existingFriendPick.userId = "user-01"
        existingFriendPick.placeId = "place-1"
        val existPicker = Picker(userId = "picker-01", profilePhoto = "songpon",
                profileName = "songpon-pic", pickTime = LocalDateTime.now().minusDays(1))
        existingFriendPick.pickers.add(existPicker)

        `when`(mockFriendPickRepository.getFriendPickByPlaceIdAndPickerId("place-01", "picker-01"))
                .thenReturn(Flux.just(existingFriendPick))
        `when`(mockFriendPickRepository.deleteFriendPick("id-1")).thenReturn(Mono.just(true))

        friendPickService.unPopulateFriendPick("picker-01", "place-01")

        verify(mockFriendPickRepository, times(1))
                .getFriendPickByPlaceIdAndPickerId("place-01", "picker-01")
        verify(mockFriendPickRepository, times(1))
                .deleteFriendPick("id-1")
    }

    @Test
    fun testUnPopulateFriendPickWithManyPicker() {
        val existingFriendPick = FriendPick()
        existingFriendPick.id = "id-1"
        existingFriendPick.userId = "user-01"
        existingFriendPick.placeId = "place-1"
        val existPicker = Picker(userId = "picker-01", profilePhoto = "songpon",
                profileName = "songpon-pic", pickTime = LocalDateTime.now().minusDays(1))
        val existPicker2 = Picker(userId = "picker-02", profilePhoto = "songpon2",
                profileName = "songpon-pic2", pickTime = LocalDateTime.now().minusDays(1))
        existingFriendPick.pickers.add(existPicker)
        existingFriendPick.pickers.add(existPicker2)

        val unPopulatedFriendPick = existingFriendPick.copy()
        unPopulatedFriendPick.pickers.remove(existPicker)

        `when`(mockFriendPickRepository.getFriendPickByPlaceIdAndPickerId("place-01", "picker-01"))
                .thenReturn(Flux.just(existingFriendPick))
        `when`(mockFriendPickRepository.saveFriendPick(unPopulatedFriendPick))
                .thenReturn(Mono.just(unPopulatedFriendPick))

        friendPickService.unPopulateFriendPick("picker-01", "place-01")

        verify(mockFriendPickRepository, times(1))
                .getFriendPickByPlaceIdAndPickerId("place-01", "picker-01")
        verify(mockFriendPickRepository, times(1))
                .saveFriendPick(unPopulatedFriendPick)
    }
}