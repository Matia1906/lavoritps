package it.lesstheory.fotoalbum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/").permitAll()        // La home è libera per tutti
                .requestMatchers("/admin/**").authenticated() // L'admin è protetto
                .anyRequest().permitAll()               // Tutto il resto (CSS, immagini) è libero
            )
            .formLogin(withDefaults()) // Attiva il form di login standard
            .logout((logout) -> logout.permitAll());

        // Serve per far funzionare il tasto "Elimina" e H2 console senza errori CSRF in locale
        http.csrf(csrf -> csrf.disable());
        
        return http.build();
    }
}
