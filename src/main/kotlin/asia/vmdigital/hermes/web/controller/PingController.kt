package asia.vmdigital.hermes.web.controller

import asia.vmdigital.hermes.domain.User
import asia.vmdigital.hermes.repository.UserRepository
import asia.vmdigital.hermes.service.FriendPickService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class PingController(private val service: FriendPickService,
                     private val repo: UserRepository) {

    @GetMapping("/ping")
    fun ping() = "pong"

    @GetMapping("test")
    fun test() : Mono<User> {

        return repo.getUser("1300")
    }
}