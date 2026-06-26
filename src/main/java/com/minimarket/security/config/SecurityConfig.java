package com.minimarket.security.config;

import com.minimarket.security.jwt.JwtAuthenticationFilter;
import com.minimarket.security.oauth.OAuth2IdaasProperties;
import com.minimarket.security.oauth.OAuth2LoginSuccessHandler;
import com.minimarket.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final OAuth2IdaasProperties oAuth2IdaasProperties;
    private final ObjectProvider<OAuth2LoginSuccessHandler> oAuth2LoginSuccessHandler;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler,
            OAuth2IdaasProperties oAuth2IdaasProperties,
            ObjectProvider<OAuth2LoginSuccessHandler> oAuth2LoginSuccessHandler
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
        this.oAuth2IdaasProperties = oAuth2IdaasProperties;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(
                        oAuth2IdaasProperties.isClientEnabled()
                                ? SessionCreationPolicy.IF_REQUIRED
                                : SessionCreationPolicy.STATELESS
                ))
                .headers(headers -> headers
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        if (oAuth2IdaasProperties.isResourceServerEnabled()) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        }

        if (oAuth2IdaasProperties.isClientEnabled()) {
            http.oauth2Login(oauth2 -> {
                oauth2.loginPage("/oauth2/authorization/" + oAuth2IdaasProperties.getRegistrationId());
                OAuth2LoginSuccessHandler successHandler = oAuth2LoginSuccessHandler.getIfAvailable();
                if (successHandler != null) {
                    oauth2.successHandler(successHandler);
                }
            });
        }

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**", "/app/index_normal", "/api/public", "/api/auth/**",
                        "/api/security/info", "/api/security/oauth2/status", "/error",
                        "/h2-console/**", "/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/app/index_protegido", "/api/private").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**")
                .hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE")
                .requestMatchers(HttpMethod.POST, "/api/carrito/**", "/api/detalle-ventas/**")
                .hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE")
                .requestMatchers(HttpMethod.POST, "/api/ventas/**")
                .hasRole("EMPLEADO")
                .requestMatchers(HttpMethod.GET, "/api/carrito/**", "/api/ventas/**", "/api/detalle-ventas/**")
                .hasAnyRole("EMPLEADO", "GERENTE")
                .requestMatchers("/api/inventario/**").hasAnyRole("EMPLEADO", "GERENTE")
                .requestMatchers("/api/security/audit/**").hasRole("GERENTE")
                .requestMatchers("/api/usuarios/**").hasRole("GERENTE")
                .requestMatchers(HttpMethod.POST, "/api/productos/**")
                .hasRole("GERENTE")
                .requestMatchers(HttpMethod.POST, "/api/categorias/**")
                .hasAnyRole("EMPLEADO", "GERENTE")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**")
                .hasRole("GERENTE")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**")
                .hasAnyRole("EMPLEADO", "GERENTE")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**", "/api/categorias/**")
                .hasRole("GERENTE")
                .anyRequest().authenticated()
        );

        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
