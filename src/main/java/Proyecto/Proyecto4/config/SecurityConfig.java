package Proyecto.Proyecto4.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import Proyecto.Proyecto4.config.CustomAuthenticationSuccessHandler;
import Proyecto.Proyecto4.config.CustomAuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class SecurityConfig {
    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;
    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    // 1. DEFINICIÓN DE RUTAS PÚBLICAS
                        .requestMatchers("/", "/nosotros", "/contactos", "/contactos/enviar", "/login", "/register", "/acercade", "/eventos",
                                "/spa", "/bodas")
                        .permitAll()
                        .requestMatchers("/css/**", "/js/**", "/imagenes/**", "/static/**", "/uploads/**").permitAll()
                        .requestMatchers("/auth/registro", "/auth/welcome", "/auth/test-connection", 
                                        "/auth/validar-email", "/auth/validar-telefono", "/auth/validar-dni").permitAll()
                    // 2. DEFINICION DE RUTAS DE ADMINISTRADOR (AUTORIZACIÓN)     
                        .requestMatchers("/api/admin/crear-super-admin").permitAll() 
                                                                                     // inicial
                        .requestMatchers("/reservas", "/reservas/buscar", "/reservas/habitacion/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    // 3. DEFINICIÓN DE RUTAS DE USUARIO AUTENTICADO (AUTORIZACIÓN)
                        .requestMatchers("/api/perfil/**").authenticated()
                        .anyRequest().authenticated())
                // 4. CONFIGURACIÓN DEL FORMULARIO DEL LOGIN Y LOGOUT
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());
        return http.build();
    }
    //BEAN PARA ENCRIPTAR CONTRASEÑAS
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
