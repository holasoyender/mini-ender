package api.routes

import database.Postgres
import jda
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Status {

    @GetMapping("/status")
    fun status(): HashMap<String, Any> {
        val response = HashMap<String, Any>()
        response["status"] = "200"
        response["health"] = HashMap<String, Any>().apply {
            this["api"] = HashMap<String, Any>().apply {
                this["status"] = "ok"
                this["version"] = "1"
            }
            this["database"] = HashMap<String, Any>().apply {
                this["status"] = if(Postgres.dataSource != null) "ok" else "error"
                this["version"] = Postgres.dataSource?.connection?.metaData?.databaseProductVersion ?: "unknown"
                this["driver"] = Postgres.dataSource?.connection?.metaData?.driverName ?: "unknown"
            }
            this["bot"] = HashMap<String, Any>().apply {
                this["status"] = jda!!.status.name
                this["ping"] = jda!!.shardManager!!.averageGatewayPing
            }
        }
        return response
    }

    @GetMapping("/shards")
    fun shards(): HashMap<String, Any> {
        val response = HashMap<String, Any>()
        response["status"] = "200"
        response["shards"] = HashMap<String, Any>().apply {
            jda!!.shardManager!!.shards.forEach {
                this[it.shardInfo.shardId.toString()] = HashMap<String, Any>().apply {
                    this["status"] = it.status.name
                    this["ping"] = it.gatewayPing
                    this["guilds"] = it.guilds.size
                    this["gateway"] = it.status.name
                }
            }
        }
        return response
    }
}