package asia.vmdigital.hermes.repository

import asia.vmdigital.hermes.domain.User
import reactor.core.publisher.Mono

interface UserRepository {

    fun getUser(userId: String): Mono<User>

    fun save(user: User): Mono<User>

    fun getUserFromRemote(userId: String): Mono<User>
}