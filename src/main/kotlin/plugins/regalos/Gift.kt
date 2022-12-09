package plugins.regalos

import org.json.JSONObject

class Gift(
    name: String,
    description: String,
    image: String,
    rarity: Int,
    chance: Int
) {

    val name: String
    val description: String
    val image: String
    val rarity: Int
    val chance: Int

    init {
        this.name = name
        this.description = description
        this.image = image
        this.rarity = rarity
        this.chance = chance
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