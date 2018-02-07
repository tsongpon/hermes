package asia.vmdigital.hermes.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class Utils {
    companion object {

        fun dateToLocalDateTime(date: Date?): LocalDateTime? {
            if (date == null) {
                return null
            }
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
        }

        fun localDateTimeToDate(localDateTime: LocalDateTime?): Date? {
            if(localDateTime == null) {
                return null
            }
            return Date.from(localDateTime.atZone(ZoneId.systemDefault())?.toInstant())
        }
    }
}