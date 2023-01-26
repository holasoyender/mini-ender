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
        startedAt = try { body["started_at"] as String? ?: "" } catch (e: Exception) { "" }

        id = try { body["id"] as String? ?: "" } catch (e: Exception) { "" }
        type = try { body["type"] as String? ?: "" } catch (e: Exception) { "" }
    }
}