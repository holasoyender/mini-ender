package interfaces

interface RedisSchema {

    fun save(): RedisSchema?
    fun delete(): RedisSchema?
    fun exists(): Boolean
}