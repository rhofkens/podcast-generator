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
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.http.RequestEntity;
import org.springframework.util.MultiValueMap;
import java.util.function.Consumer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
                                         ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, 
                "/oauth2/authorization"
            );

        // Configure PKCE
        authorizationRequestResolver.setAuthorizationRequestCustomizer(
            customizer -> customizer
                .attributes(attrs -> {
                    attrs.put(PkceParameterNames.CODE_CHALLENGE_METHOD, "S256");
                    attrs.put("enablePkce", true);
                })
                .additionalParameters(params -> {
                    params.put("code_challenge_method", "S256");
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
                .tokenEndpoint(token -> token
                    .accessTokenResponseClient(accessTokenResponseClient())
                )
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
            );
        
        return http.build();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        
        client.setRequestEntityConverter(new OAuth2AuthorizationCodeGrantRequestEntityConverter() {
            @Override
            public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest oauth2Request) {
                RequestEntity<?> entity = super.convert(oauth2Request);
                MultiValueMap<String, String> params = (MultiValueMap<String, String>) entity.getBody();
                
                // Ensure PKCE parameters are included
                OAuth2AuthorizationRequest authorizationRequest = oauth2Request.getAuthorizationExchange()
                    .getAuthorizationRequest();
                String codeVerifier = authorizationRequest.getAttribute(PkceParameterNames.CODE_VERIFIER);
                if (codeVerifier != null) {
                    params.add(PkceParameterNames.CODE_VERIFIER, codeVerifier);
                }
                
                return new RequestEntity<>(params, entity.getHeaders(), entity.getMethod(), entity.getUrl());
            }
        });
        
        return client;
    }
}
