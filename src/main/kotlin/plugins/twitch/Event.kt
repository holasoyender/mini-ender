package plugins.twitch

import org.json.JSONObject


class Event(
    body: JSONObject
) {

    val broadcasterUserId: String
    val broadcasterUserLogin: String
    val broadcasterUserName: String
    val startedAt: String

    val id: String
    val type: String

    init {
        broadcasterUserId = body["broadcaster_user_id"] as String? ?: ""
        broadcasterUserLogin = body["broadcaster_user_login"] as String? ?: ""
        broadcasterUserName = body["broadcaster_user_name"] as String? ?: ""
        startedAt = body["started_at"] as String? ?: ""

        id = body["id"] as String? ?: ""
        type = body["type"] as String? ?: ""
    }
}