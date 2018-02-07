package asia.vmdigital.hermes.repository

import java.util.*

class KeyGenerator {
    companion object {
        fun getKey(): String {
            return UUID.randomUUID().toString()
        }
    }
}