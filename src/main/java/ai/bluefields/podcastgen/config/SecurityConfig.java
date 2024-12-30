package ai.bluefields.podcastgen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import java.util.function.Consumer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
                                         ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        // Create custom authorization request resolver with PKCE
        DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, 
                "/oauth2/authorization"
            );

        // Enable PKCE
        authorizationRequestResolver.setAuthorizationRequestCustomizer(
            customizer -> customizer.attributes(attrs -> {
                attrs.put(OAuth2AuthorizationRequest.CODE_CHALLENGE_METHOD_ATTRIBUTE_NAME, "S256");
                attrs.put(OAuth2AuthorizationRequest.CODE_CHALLENGE_ATTRIBUTE_NAME, "REQUIRED");
            })
        );

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/assets/**", "/favicon.ico", "/login", "/login/**", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(authorizationRequestResolver)
                )
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
            );
        
        return http.build();
    }
}
