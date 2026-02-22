package com.tpt.apfc.config;

import com.tpt.apfc.bean.LoginUser;
import com.tpt.apfc.bean.LoginUserEntity;
import com.tpt.apfc.service.LoginUserDbService;
import com.tpt.apfc.service.MockLoginAttemptTracker;
import com.tpt.apfc.service.MockLoginUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {


    @Bean
    @Profile("mock")
    public AuthenticationProvider mockAuthenticationProvider(
            MockLoginUserService mockLoginUserService,
            PasswordEncoder passwordEncoder
    ) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String rawPassword = String.valueOf(authentication.getCredentials());

                LoginUser u = mockLoginUserService.findByAccount(username)
                        .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

                if (u.getStatus() != null && u.getStatus() == 3) throw new DisabledException("User deleted");
                if (u.getStatus() != null && u.getStatus() == 2) throw new LockedException("User locked");

                if (u.getPasswordHash() == null || !passwordEncoder.matches(rawPassword, u.getPasswordHash())) {
                    throw new BadCredentialsException("Bad credentials");
                }

                List<GrantedAuthority> authorities = switch (u.getAccount()) {
                    case "admin" -> List.of(
                            new SimpleGrantedAuthority("ROLE_USERLOOKUP"),
                            new SimpleGrantedAuthority("ROLE_HELLO")
                    );
                    case "hello" -> List.of(new SimpleGrantedAuthority("ROLE_HELLO"));
                    default -> List.of(new SimpleGrantedAuthority("ROLE_USERLOOKUP"));
                };

                return new UsernamePasswordAuthenticationToken(username, rawPassword, authorities);
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }

    @Bean
    @Profile("!mock")
    public AuthenticationProvider dbAuthenticationProvider(
            LoginUserDbService loginUserDbService,
            PasswordEncoder passwordEncoder
    ) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String rawPassword = String.valueOf(authentication.getCredentials());

                LoginUserEntity u = loginUserDbService.findByAccount(username)
                        .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

                if (u.getStatus() != null && u.getStatus() == 3) throw new DisabledException("User deleted");
                if (u.getStatus() != null && u.getStatus() == 2) throw new LockedException("User locked");

                if (u.getPasswordHash() == null || !passwordEncoder.matches(rawPassword, u.getPasswordHash())) {
                    throw new BadCredentialsException("Bad credentials");
                }

                List<GrantedAuthority> authorities = switch (u.getAccount()) {
                    case "admin" -> List.of(
                            new SimpleGrantedAuthority("ROLE_USERLOOKUP"),
                            new SimpleGrantedAuthority("ROLE_HELLO")
                    );
                    case "hello" -> List.of(new SimpleGrantedAuthority("ROLE_HELLO"));
                    default -> List.of(new SimpleGrantedAuthority("ROLE_USERLOOKUP"));
                };

                return new UsernamePasswordAuthenticationToken(username, rawPassword, authorities);
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }

    @Bean
    @Profile("mock")
    public SecurityFilterChain mockSecurityFilterChain(HttpSecurity http, MockLoginAttemptTracker attemptTracker) throws Exception {
        return base(http)
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler((req, resp, auth) -> {
                            attemptTracker.recordSuccess(auth.getName());
                            try {
                                jsonOk(resp);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .failureHandler((req, resp, ex) -> {
                            String username = extractUsername(req);
                            if (username != null && !username.isBlank()) attemptTracker.recordFailure(username);
                            try {
                                jsonLoginFailed(resp);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((req, resp, auth) -> resp.setStatus(HttpServletResponse.SC_OK))
                )
                .build();
    }

    @Bean
    @Profile("!mock")
    public SecurityFilterChain dbSecurityFilterChain(HttpSecurity http, LoginUserDbService loginUserDbService) throws Exception {
        return base(http)
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler((req, resp, auth) -> {
                            loginUserDbService.recordLoginSuccess(auth.getName());
                            try {
                                jsonOk(resp);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .failureHandler((req, resp, ex) -> {
                            String username = extractUsername(req);
                            if (username != null && !username.isBlank()) loginUserDbService.recordLoginFailure(username);
                            try {
                                jsonLoginFailed(resp);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((req, resp, auth) -> resp.setStatus(HttpServletResponse.SC_OK))
                )
                .build();
    }

    private static HttpSecurity base(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/*.ico",
                                "/*.png",
                                "/*.svg",
                                "/*.css",
                                "/*.js"
                        ).permitAll()
                        .requestMatchers("/api/auth/me").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                );
        return http;
    }

    private static String extractUsername(HttpServletRequest req) {
        return req.getParameter("username");
    }

    private static void jsonOk(HttpServletResponse resp) throws Exception {
        resp.setStatus(200);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write("{\"ok\":true}");
    }

    private static void jsonLoginFailed(HttpServletResponse resp) throws Exception {
        resp.setStatus(401);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write("{\"ok\":false,\"message\":\"LOGIN_FAILED\"}");
    }

}