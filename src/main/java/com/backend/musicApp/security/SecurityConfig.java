package com.backend.musicApp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collection;
import java.util.Map;



@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {
    /**
     * Configures CORS (Cross-Origin Resource Sharing) for the application.
     *
     * @param registry The CORS registry to configure.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }

    /**
     * Configures the security filter chain for the application.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/").permitAll()
                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(request -> {
                            String accessToken = request.getHeader("Authorization");
                            if (accessToken != null) {
                                accessToken = accessToken.substring("Bearer ".length());
                                return accessToken;
                            }
                            return new DefaultBearerTokenResolver().resolve(request);
                        })
                        .jwt(jwtConfigurer -> {
                            jwtConfigurer.decoder(JwtDecoders.fromOidcIssuerLocation("http://localhost:9000/realms/musicAppRealm"));
                            jwtConfigurer.jwtAuthenticationConverter(jwt -> {
                                Map<String, Collection<String>> realmAccess = jwt.getClaim("realm_access");
                                Collection<String> roles = realmAccess.get("roles");
                                var grantedAuthorities = roles.stream()
                                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                        .toList();
                                return new JwtAuthenticationToken(jwt, grantedAuthorities);
                            });
                        }
                        ));
        return http.build();
    }
}
