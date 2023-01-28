package api.handlers

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class ErrorController : ErrorController {
    @RequestMapping("/error")
    fun getErrorPage(request: HttpServletRequest): ResponseEntity<HashMap<String, Any>> {

        val status = Integer.valueOf(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)?.toString() ?: "404") ?: 404

        val errorMessage = when (status) {
            in 400 .. 499 -> "Bad Request"
            in 500 .. 599 -> "Internal Server Error"
            else -> "Error ${request.getAttribute("javax.servlet.error.status_code")?.toString() ?: "Desconocido"}"
        }

        val response: HashMap<String, Any> = HashMap()
        response["status"] = status
        response["message"] = errorMessage

        return ResponseEntity.status(status).body(response)
    }
}