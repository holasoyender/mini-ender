package cache

class MessageCacheObject(
    content: String,
    contentDisplay: String,
    id: String,
    authorTag: String,
    authorId: String,
    authorAvatar: String,
    channelId: String,
) {

    val content: String
    val contentDisplay: String
    val id: String
    val authorTag: String
    val authorId: String
    val authorAvatar: String
    val channelId: String

    init {
        this.content = content
        this.contentDisplay = contentDisplay
        this.id = id
        this.authorTag = authorTag
        this.authorId = authorId
        this.authorAvatar = authorAvatar
        this.channelId = channelId
    }
}