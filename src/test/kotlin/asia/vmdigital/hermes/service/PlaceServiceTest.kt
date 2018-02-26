package asia.vmdigital.hermes.service

import asia.vmdigital.hermes.domain.Place
import asia.vmdigital.hermes.query.PlaceQuery
import asia.vmdigital.hermes.repository.PlaceRepositoryMongoReaciveImpl
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito
import org.junit.Assert
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.data.geo.Point
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class PlaceServiceTest {

    private val placeRepository = Mockito.mock(PlaceRepositoryMongoReaciveImpl::class.java)
    private val friendPickService = Mockito.mock(FriendPickService::class.java)

    private val placeService = PlaceService(placeRepository, friendPickService)

    @Test
    fun testSaveNewPlace() {
        val placeToSave = Place()
        placeToSave.userId = "user-1"
        placeToSave.placeId = "place-1"
        placeToSave.type = "Google"
        placeToSave.location  = Point(123.0, 456.0)

        val savedPlace = Place()
        savedPlace.id = "id-1"
        savedPlace.userId = "user-1"
        savedPlace.placeId = "place-1"
        savedPlace.type = "Google"
        savedPlace.location  = Point(123.0, 456.0)
        savedPlace.createTime = LocalDateTime.now()
        savedPlace.updateTime = LocalDateTime.now()

        `when`(placeRepository.getPlaceBy("user-1", "place-1")).thenReturn(Mono.empty())
        `when`(placeRepository.savePlace(placeToSave)).thenReturn(Mono.just(savedPlace))

        val place = placeService.savePlace(placeToSave).block()
        Assert.assertNotNull(place)
        Assert.assertNotNull(place!!.id)
        Assert.assertEquals("user-1", place.userId)

        Mockito.verify(placeRepository, times(1)).getPlaceBy("user-1", "place-1")
        Mockito.verify(placeRepository, times(1)).savePlace(placeToSave)
        Mockito.verify(friendPickService, times(1))
                .populateFriendPick("user-1", savedPlace, savedPlace.updateTime!!, "Google")
    }

    @Test
    fun testUpdatePlace() {
        val placeToSave = Place()
        placeToSave.id = "id-1"
        placeToSave.userId = "user-1"
        placeToSave.placeId = "place-1"
        placeToSave.type = "Google"
        placeToSave.location  = Point(123.0, 456.0)

        val yesterday = LocalDateTime.now().minusDays(1)
        val existingPlace = Place()
        existingPlace.id = "id-1"
        existingPlace.userId = "user-1"
        existingPlace.placeId = "place-1"
        existingPlace.type = "Google"
        existingPlace.location  = Point(123.0, 456.0)
        existingPlace.createTime = yesterday
        existingPlace.updateTime = yesterday

        val savedPlace = Place()
        savedPlace.id = "id-1"
        savedPlace.userId = "user-1"
        savedPlace.placeId = "place-1"
        savedPlace.type = "Google"
        savedPlace.location  = Point(123.0, 456.0)
        savedPlace.createTime = yesterday
        savedPlace.updateTime = LocalDateTime.now()

        val mergedPlace = Place()
        mergedPlace.id = "id-1"
        mergedPlace.userId = "user-1"
        mergedPlace.placeId = "place-1"
        mergedPlace.type = "Google"
        mergedPlace.location  = Point(123.0, 456.0)
        mergedPlace.createTime = yesterday

        `when`(placeRepository.getPlaceBy("user-1", "place-1")).thenReturn(Mono.just(existingPlace))
        `when`(placeRepository.savePlace(placeToSave)).thenReturn(Mono.just(savedPlace))

        val place = placeService.savePlace(placeToSave).block()
        Assert.assertNotNull(place)
        Assert.assertNotNull(place!!.id)
        Assert.assertEquals("user-1", place.userId)

        verify(placeRepository, times(1)).getPlaceBy("user-1", "place-1")
        verify(placeRepository, times(1)).savePlace(mergedPlace)
        verify(friendPickService, times(1))
                .populateFriendPick("user-1", savedPlace, savedPlace.updateTime!!, "Google")
    }

    @Test
    fun testListPlaces() {
        val queryResult = Flux.just(Place(userId = "user-1",
                placeId = "place-1",
                type = "Google",
                location = Point(123.0, 456.0),
                createTime = LocalDateTime.now(),
                updateTime = LocalDateTime.now()
        ), Place(userId = "user-1",
                placeId = "place-2",
                type = "Google",
                location = Point(661.0, 987.0),
                createTime = LocalDateTime.now(),
                updateTime = LocalDateTime.now()))

        val query = PlaceQuery(userId = "user-1", size = 10, start = 2)
        `when`(placeRepository.queryPlaces(query)).thenReturn(queryResult)

        val result = placeService.listPlaces(query).collectList().block()
        Assert.assertEquals(2, result!!.size)

        verify(placeRepository, times(1)).queryPlaces(query)
    }

    @Test
    fun testListNotExistPlaces() {
        val queryResult = Flux.empty<Place>()
        val query = PlaceQuery(userId = "user-1", size = 10, start = 2)
        `when`(placeRepository.queryPlaces(query)).thenReturn(queryResult)

        val resultFlux = placeService.listPlaces(query).collectList()

        Assert.assertNotNull(resultFlux)

        val result = resultFlux.block()
        Assert.assertEquals(0, result!!.size)
        verify(placeRepository, times(1)).queryPlaces(query)
    }

    @Test
    fun testCountPlaces() {
        val query = PlaceQuery(userId = "user-1", size = 10, start = 2)
        `when`(placeRepository.countPlaces(query)).thenReturn(Mono.just(2))

        val result = placeService.countPlace(query).block()
        Assert.assertEquals(2, result!!)

        verify(placeRepository, times(1)).countPlaces(query)
    }

    @Test
    fun testGetPlace() {
        val result = Place(id = "id-1", userId = "user-1", placeId = "place-1")
        `when`(placeRepository.getPlace("id-1")).thenReturn(Mono.just(result))

        val place = placeService.getPlace("id-1").block()

        Assert.assertNotNull(place)
        Assert.assertEquals("id-1", place!!.id)

        verify(placeRepository, times(1)).getPlace("id-1")
    }

    @Test
    fun testGetNotExistPlace() {
        `when`(placeRepository.getPlace("id-1")).thenReturn(Mono.empty())

        val place = placeService.getPlace("id-1").block()
        Assert.assertNull(place)
        verify(placeRepository, times(1)).getPlace("id-1")
    }

    @Test
    fun testDeletePlace() {
        val mockPlace = Place(id="id-1", placeId = "place-1", userId = "user-1")
        `when`(placeRepository.getPlace("id-1")).thenReturn(Mono.just(mockPlace))
        `when`(placeRepository.deletePlace("id-1")).thenReturn(Mono.just(true))
        val result = placeService.deletePlace("user-1", "id-1").block()
        Assert.assertTrue(result!!)
        verify(placeRepository, times(1)).deletePlace("id-1")
        verify(friendPickService, times(1)).unPopulateFriendPick("user-1", "place-1")
    }

    @Test
    fun testDeleteNotExistPlace() {
        `when`(placeRepository.getPlace("id-1")).thenReturn(Mono.empty())
        val result = placeService.deletePlace("user-1", "id-1").block()
        Assert.assertNull(result)
        verify(placeRepository, times(0)).deletePlace("id-1")
        verify(friendPickService, times(0)).unPopulateFriendPick("user-1", "place-1")
    }
}