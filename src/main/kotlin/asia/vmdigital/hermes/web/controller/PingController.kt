package asia.vmdigital.hermes.web.controller

import asia.vmdigital.hermes.domain.User
import asia.vmdigital.hermes.repository.UserRepositoryImpl
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class PingController(private val repo: UserRepositoryImpl) {

    @GetMapping("/ping")
    fun ping() = "pong"

    @GetMapping("test")
    fun test(): Mono<User> {
        return repo.getUser("123321")
    }
}