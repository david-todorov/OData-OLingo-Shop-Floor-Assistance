package com.shopfloor.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration class for security settings.
 * Ensures the authentication policies and CORS configurations.
 * @author David Todorov (https://github.com/david-todorov)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructor for SecurityConfig.
     *
     * @param jwtAuthenticationFilter the JWT authentication filter
     * @param authenticationProvider the authentication provider
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures the security filter chain for HTTP requests.
     * Disables CSRF, sets authorization rules for different endpoints based on user roles,
     * enforces stateless session management, and adds a JWT authentication filter.
     *
     * There are two hierarchical roles in this setup:
     * - **EDITOR**: Has access to both editors and operators orders.
     * - **OPERATOR**: Has limited access to operators orders only.
     *
     * **Note**: If a new endpoint or role is introduced, this method will need to be updated to reflect the new access rules.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Add this line
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/editor/**").hasRole("EDITOR")
                        .requestMatchers("/operator/**").hasAnyRole("EDITOR", "OPERATOR")
                        .requestMatchers("/odata/**").permitAll()// In future this may be changed to hasRole("OLINGO")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) settings for the application,
     * allowing specific origins, HTTP methods, and headers to be used in cross-origin requests.
     *
     * - **Allowed Origins**: Only requests from `http://localhost:4200` and `http://localhost:80` are permitted.
     * - **Allowed Methods**: Limits cross-origin HTTP requests to "GET", "POST", "PUT", and "DELETE".
     * - **Allowed Headers**: Restricts allowed headers to "Authorization" and "Content-Type".
     * - **Allow Credentials**: Enables sending of credentials (such as cookies or authorization headers).
     * - **Configuration Application**: Applies this configuration to all endpoints (`/**`).
     *
     * @return a CORS configuration source with specified settings
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:80", "http://localhost")); // Add http://localhost
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // Allow specific HTTP methods
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Allow specific headers
        configuration.setAllowCredentials(true); // Allow credentials for authorization headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply the configuration to all endpoints

        return source;
    }
}
