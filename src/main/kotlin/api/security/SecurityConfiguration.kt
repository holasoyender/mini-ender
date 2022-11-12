@file:Suppress("DEPRECATION")

package api.security

import api.security.OAuth2UserAgentUtils.withUserAgent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.RequestEntity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import java.util.*

@Configuration
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {

        http/*.antMatcher("/**")
            .authorizeRequests().antMatchers("/", "/login**").permitAll().anyRequest().authenticated().and()
            .exceptionHandling()
            .authenticationEntryPoint(LoginUrlAuthenticationEntryPoint("/")).and().logout().logoutSuccessUrl("/").and()*/*/
            .csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
            .oauth2Login()
            .tokenEndpoint()
            .accessTokenResponseClient(accessTokenResponseClient())
            .and()
            .userInfoEndpoint().userService(userService())
    }

    @Bean
    fun accessTokenResponseClient(): OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
        val client = DefaultAuthorizationCodeTokenResponseClient()
        client.setRequestEntityConverter(object : OAuth2AuthorizationCodeGrantRequestEntityConverter() {
            override fun convert(oauth2Request: OAuth2AuthorizationCodeGrantRequest?): RequestEntity<*> {
                return withUserAgent(super.convert(oauth2Request)!!)
            }
        })
        return client
    }

    @Bean
    fun userService(): OAuth2UserService<OAuth2UserRequest, OAuth2User> {
        val service = DefaultOAuth2UserService()
        service.setRequestEntityConverter(object : OAuth2UserRequestEntityConverter() {
            override fun convert(userRequest: OAuth2UserRequest): RequestEntity<*> {
                return withUserAgent(super.convert(userRequest)!!)
            }
        })
        return service
    }
}