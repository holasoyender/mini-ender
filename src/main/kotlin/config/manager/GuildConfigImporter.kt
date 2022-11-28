package config.manager

import interfaces.CommandResponse
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message.Attachment
import org.yaml.snakeyaml.Yaml

object GuildConfigImporter {

    fun import(file: Attachment, guild: Guild): CommandResponse {

        if (file.fileExtension != "yaml" || file.fileExtension != "yml") return CommandResponse.error("El archivo adjuntado no es un archivo YAML")
        if (file.fileName != "${guild.id}.yaml" || file.fileName != "${guild.id}.yml") return CommandResponse.error("El archivo adjuntado no es el archivo de configuración de este servidor")
        if (file.size > 1000000) return CommandResponse.error("El archivo es demasiado grande, el tamaño máximo es de 1MB")

        try {
            val content = file.proxy.download().get().readAllBytes().toString(Charsets.UTF_8)

            if(content.length < 100) return CommandResponse.error("El archivo está vacío o no es válido")

            val config = Yaml().load(content) as Map<String, Any>
            val (isValid, error) = GuildConfigValidator(config).validate()
            if(!isValid) return CommandResponse.error("El archivo no es válido: `\n```$error``")

            return CommandResponse.success()

        } catch (e: Exception) {
            return CommandResponse.error("El archivo de configuración no es valido: `\n```${e.message ?: "Error desconocido"}``")
        }
    }
}