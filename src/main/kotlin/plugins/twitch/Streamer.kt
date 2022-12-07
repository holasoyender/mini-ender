package plugins.twitch

class Streamer(
    id: String,
    login: String,
    displayName: String,
    type: String,
    broadcasterType: String,
    description: String,
    profileImageUrl: String,
    offlineImageUrl: String,
    viewCount: Int
) {

    val id: String
    val login: String
    val displayName: String
    val type: String
    val broadcasterType: String
    val description: String
    val profileImageUrl: String
    val offlineImageUrl: String
    val viewCount: Int

    init {
        this.id = id
        this.login = login
        this.displayName = displayName
        this.type = type
        this.broadcasterType = broadcasterType
        this.description = description
        this.profileImageUrl = profileImageUrl
        this.offlineImageUrl = offlineImageUrl
        this.viewCount = viewCount
    }
}