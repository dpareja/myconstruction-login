package com.myconstruction.myconstruction_login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.SecurityFilterChain;

import com.myconstruction.myconstruction_login.service.JpaUserDetailsService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("ENCODER: Usando BCrypt puro");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        System.out.println("DEPURACIÓN: UserDetailsService creado (JpaUserDetailsService)");
        return new JpaUserDetailsService();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        System.out.println("DEPURACIÓN: DaoAuthenticationProvider creado y configurado");
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("DEPURACIÓN: Configurando SecurityFilterChain...");

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
                .successHandler((request, response, authentication) -> {
                    System.out.println("LOGIN ÉXITO!");
                    System.out.println("   → Usuario autenticado: " + authentication.getName());
                    System.out.println("   → Autoridades: " + authentication.getAuthorities());
                    response.sendRedirect("/home");
                })
                .failureHandler((request, response, exception) -> {
                    String username = request.getParameter("username");
                    System.out.println("LOGIN FALLÓ!");
                    System.out.println("   → Usuario: " + username);
                    System.out.println("   → Error: " + exception.getMessage());
                    if (exception instanceof BadCredentialsException) {
                        System.out.println("   → CAUSA: Contraseña incorrecta o hash corrupto");
                    }
                    response.sendRedirect("/login?error");
                })
            )
            .logout(logout -> logout
                .logoutSuccessHandler((req, res, auth) -> {
                    System.out.println("LOGOUT: Usuario " + (auth != null ? auth.getName() : "anónimo") + " cerró sesión");
                    res.sendRedirect("/");
                })
                .permitAll()
            )
            .authenticationProvider(authenticationProvider());

        System.out.println("DEPURACIÓN: SecurityFilterChain configurado correctamente");
        return http.build();
    }
}