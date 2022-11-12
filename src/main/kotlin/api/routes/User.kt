package api.routes

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class User {

    @GetMapping("/user/me")
    fun getCurrentLoggedInUser(): String {
        val user = SecurityContextHolder.getContext().authentication.principal
        return user.toString()

    }
}