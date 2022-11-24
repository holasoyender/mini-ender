package plugins.twitch

import org.json.JSONObject

class TwitchEventBody(
    body: JSONObject
) {
    val subscription: Subscription
    val event: Event

    init {
        subscription = Subscription(body["subscription"] as JSONObject)
        event = Event(body["event"] as JSONObject)
    }
}
