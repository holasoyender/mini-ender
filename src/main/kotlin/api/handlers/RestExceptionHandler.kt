package api.handlers

import org.springframework.http.HttpStatus
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException


@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNoHandlerFound(e: NoHandlerFoundException, request: WebRequest?): HashMap<String, String>? {
        val response: HashMap<String, String> = HashMap()
        response["status"] = "404"
        response["message"] = e.localizedMessage
        response["path"] = e.requestURL
        response["method"] = e.httpMethod
        return response
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    fun handleMethodNotAllowed(e: HttpRequestMethodNotSupportedException, request: WebRequest?): HashMap<String, String>? {
        val response: HashMap<String, String> = HashMap()
        response["status"] = "405"
        response["message"] = e.localizedMessage
        response["method"] = e.method
        response["supported_methods"] = e.supportedHttpMethods?.joinToString { it.toString() } ?: "null"
        return response
    }


    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception, request: WebRequest?): HashMap<String, String>? {
        val response: HashMap<String, String> = HashMap()
        response["status"] = "500"
        response["message"] = e.localizedMessage

        e.printStackTrace()
        return response
    }
}