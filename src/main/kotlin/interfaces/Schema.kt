package interfaces

interface Schema {

    val tableName: String

    fun dropTable()
    fun save(): Schema
    fun delete(): Schema
    fun exists(): Boolean

}