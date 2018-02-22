package asia.vmdigital.hermes.query

data class FriendPickQuery(val userId: String? = null,
                           val placeId: String? = null,
                           val size: Int = 5,
                           val start: Int = 0)