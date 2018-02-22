package asia.vmdigital.hermes.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(@Id
                var id: String? = null,
                var profileName: String? = null,
                var profilePhoto: String? = null,
                @Indexed
                var followers: ArrayList<Follower>? = null)
