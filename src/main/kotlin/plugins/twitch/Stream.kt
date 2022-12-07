package plugins.twitch

class Stream(
    id: String,
    userId: String,
    userLogin: String,
    userName: String,
    gameId: String,
    gameName: String,
    type: String,
    title: String,
    viewerCount: Int,
    startedAt: String,
    language: String,
    thumbnailUrl: String
) {

    val id: String
    val userId: String
    val userLogin: String
    val userName: String
    val gameId: String
    val gameName: String
    val type: String
    val title: String
    val viewerCount: Int
    val startedAt: String
    val language: String
    val thumbnailUrl: String

    init {
        this.id = id
        this.userId = userId
        this.userLogin = userLogin
        this.userName = userName
        this.gameId = gameId
        this.gameName = gameName
        this.type = type
        this.title = title
        this.viewerCount = viewerCount
        this.startedAt = startedAt
        this.language = language
        this.thumbnailUrl = thumbnailUrl
    }

}