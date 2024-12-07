package com.playground.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        httpSecurity.authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                        .requestMatchers("/graphql").permitAll()
                        .anyRequest().authenticated());

        httpSecurity.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler));

        // Enable basic HTTP authentication
        httpSecurity.httpBasic(withDefaults());

        return httpSecurity.build();
    }
}
