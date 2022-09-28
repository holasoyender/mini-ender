package interfaces

class CommandResponse(exitStatus: Int, error: String?) {
    var exitStatus: Int = 0
    var error: String? = null

    init {
        this.exitStatus = exitStatus
        this.error = error
    }

    companion object {

        fun success() = CommandResponse(0, null)
        fun error(error: String) = CommandResponse(1, error)
        fun error(error: String, exitStatus: Int) = CommandResponse(exitStatus, error)

    }
}