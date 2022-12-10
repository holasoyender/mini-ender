package plugins.regalos

import net.dv8tion.jda.api.entities.Member
import org.json.JSONObject

class Gift(
    name: String,
    description: String,
    image: String,
    rarity: Int,
    chance: Int,
    hook: ((member: Member?) -> Unit)? = null
) {

    val name: String
    val description: String
    val image: String
    val rarity: Int
    val chance: Int
    val hook: ((member: Member?) -> Unit)?

    init {
        this.name = name
        this.description = description
        this.image = image
        this.rarity = rarity
        this.chance = chance
        this.hook = hook
    }

    fun format(): JSONObject {
        return JSONObject()
            .put("name", name)
            .put("description", description)
            .put("image", image)
            .put("rarity", rarity)
            .put("chance", chance)
            .put("openedAt", System.currentTimeMillis())
    }
}