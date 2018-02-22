package asia.vmdigital.hermes.web.v1.controller

import asia.vmdigital.hermes.web.pagination.PaginationUtil
import asia.vmdigital.hermes.query.PlaceQuery
import asia.vmdigital.hermes.service.PlaceService
import asia.vmdigital.hermes.web.v1.mapper.PlaceTransportMapper
import asia.vmdigital.hermes.web.v1.transport.PlaceTransport
import asia.vmdigital.hermes.web.v1.transport.ResponseTransportWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("saved/v1")
class PlaceController @Autowired constructor(private val placeService: PlaceService) {

    val logger: Logger = LoggerFactory.getLogger(PlaceController::class.java)

    @GetMapping("/user/{userId}/saved")
    fun getPlaces(request: ServerHttpRequest,
                  @PathVariable("userId") userId: String,
                  @RequestParam(defaultValue = "30") size: Int,
                  @RequestParam(defaultValue = "1") page: Int): Mono<ResponseTransportWrapper<PlaceTransport>> {

        logger.debug("Listing user place")
        val query = PlaceQuery(userId = userId, size = size, start = size * (page - 1))
        val countMono = placeService.countPlace(query)
        val responseMono = placeService.listPlaces(query).map({
            PlaceTransportMapper.map(it)
        }).collectList().map { compose(it) }

        return Mono.zip(responseMono, countMono).map {
            composeResponseWithPaginate(it.t1, it.t2,
                    request.path.value(), page, size)
        }
    }

    @GetMapping("/user/{userId}/saved/{id}")
    fun getPlace(@PathVariable("id") id: String): Mono<ResponseEntity<PlaceTransport>> {
        return placeService.getPlace(id).map { ResponseEntity.ok(PlaceTransportMapper.map(it)) }
                .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @PostMapping("/user/{userId}/saved")
    fun savePlace(@RequestBody transport: Mono<PlaceTransport>,
                  @PathVariable("userId") userId: String): Mono<ResponseEntity<PlaceTransport>> {

        transport.map {  }
        return transport.flatMap({
            logger.debug("Getting request to save place {}", it)
            it.userId = userId
            placeService.savePlace(PlaceTransportMapper.map(it))
                    .map { saved -> ResponseEntity.ok().body(PlaceTransportMapper.map(saved)) }
        })
    }

    @DeleteMapping("/user/{userId}/saved/{id}")
    fun delete(@PathVariable("userId")userId: String,
               @PathVariable("id")id: String): Mono<ResponseEntity<Void>> {
        return placeService.deletePlace(userId, id).map { deleted ->
            if(deleted) {
                ResponseEntity.ok().build<Void>()
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
        }
    }

    private fun composeResponseWithPaginate(response: ResponseTransportWrapper<PlaceTransport>,
                                            total: Long,
                                            requestedUri: String,
                                            page: Int,
                                            size: Int)
            : ResponseTransportWrapper<PlaceTransport> {

        val pagination = PaginationUtil.composePagination(requestedUri, page, size, total.toInt())
        response.next = pagination.next
        response.first = pagination.first
        response.last = pagination.last
        response.previous = pagination.previous
        return response
    }

    private fun compose(transport: List<PlaceTransport>): ResponseTransportWrapper<PlaceTransport> {
        val response = ResponseTransportWrapper<PlaceTransport>()
        response.result = transport
        return response
    }

}