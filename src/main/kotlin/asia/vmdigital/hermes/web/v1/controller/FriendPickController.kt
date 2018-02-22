package asia.vmdigital.hermes.web.v1.controller

import asia.vmdigital.hermes.query.FriendPickQuery
import asia.vmdigital.hermes.service.FriendPickService
import asia.vmdigital.hermes.web.pagination.PaginationUtil
import asia.vmdigital.hermes.web.v1.mapper.FriendPickTransportMapper
import asia.vmdigital.hermes.web.v1.transport.FriendPickTransport
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/friendpicks/v1")
class FriendPickController @Autowired constructor(private val friendPickService: FriendPickService) {

    private val logger: Logger = LoggerFactory.getLogger(FriendPickController::class.java)

    @GetMapping("/user/{userId}/friendpicks")
    fun getFriendPicks(request: ServerHttpRequest,
                       @PathVariable("userId") userId: String,
                       @RequestParam(defaultValue = "30") size: Int,
                       @RequestParam(defaultValue = "1") page: Int): Mono<ResponseTransportWrapper<FriendPickTransport>> {

        logger.debug("Listing user place")
        val query = FriendPickQuery(userId = userId, size = size, start = size * (page - 1))
        val count = friendPickService.count(query)
        val responseMono = friendPickService.queryFriendPicks(query).map({
            FriendPickTransportMapper.map(it)
        }).collectList().map { compose(it) }

        return Mono.zip(responseMono, count).map {
            composeResponseWithPaginate(it.t1, it.t2,
                    request.path.value(), page, size)
        }
    }

    @DeleteMapping("/user/{userId}/friendpicks/{friendPickId}")
    fun delete(@PathVariable("friendPickId") friendPickId: String): Mono<ResponseEntity<Void>> {
        logger.debug("Deleting friendPick id {}", friendPickId)
        return friendPickService.delete(friendPickId).map { deleted ->
            if(deleted) {
                ResponseEntity.ok().build<Void>()
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
        }
    }

    private fun composeResponseWithPaginate(response: ResponseTransportWrapper<FriendPickTransport>,
                                            total: Long,
                                            requestedUri: String,
                                            page: Int,
                                            size: Int)
            : ResponseTransportWrapper<FriendPickTransport> {

        val pagination = PaginationUtil.composePagination(requestedUri, page, size, total.toInt())
        response.next = pagination.next
        response.first = pagination.first
        response.last = pagination.last
        response.previous = pagination.previous
        return response
    }

    private fun compose(transport: List<FriendPickTransport>): ResponseTransportWrapper<FriendPickTransport> {
        val response = ResponseTransportWrapper<FriendPickTransport>()
        response.result = transport
        return response
    }
}