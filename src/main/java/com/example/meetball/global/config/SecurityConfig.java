package com.example.meetball.global.config;

import com.example.meetball.global.auth.SessionProfileAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final SessionProfileAuthenticationFilter sessionProfileAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/register",
                                "/error",
                                "/mypage",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/h2-console/**",
                                "/projects/**",
                                "/people/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/projects",
                                "/api/projects/*",
                                "/api/projects/*/comments",
                                "/api/projects/*/resources",
                                "/api/projects/*/bookmarks",
                                "/api/projects/*/reviews/summary"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                        .referrerPolicy(referrerPolicy -> referrerPolicy
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER_WHEN_DOWNGRADE))
                        .crossOriginOpenerPolicy(crossOriginOpenerPolicy -> crossOriginOpenerPolicy
                                .policy(CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN_ALLOW_POPUPS))
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                )
                .addFilterBefore(sessionProfileAuthenticationFilter, AuthorizationFilter.class);

        return http.build();
    }
}
