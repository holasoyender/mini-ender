package plugins.twitch

import org.json.JSONObject

class Subscription(
    body: JSONObject
) {
    val id: String
    val status: String
    val type: String
    val version: String
    val cost: Int
    val condition: TwitchCondition
    val createdAt: String

    init {
        id = body["id"] as String? ?: ""
        status = body["status"] as String? ?: ""
        type = body["type"] as String? ?: ""
        version = body["version"] as String? ?: ""
        cost = body["cost"] as Int? ?: 0
        condition = TwitchCondition(body["condition"] as JSONObject)
        createdAt = body["created_at"] as String? ?: ""
    }

    class TwitchCondition(
        body: JSONObject
    ) {
        val broadcasterUserId: String

        init {
            broadcasterUserId = body["broadcaster_user_id"] as String? ?: ""
        }
    }
}
