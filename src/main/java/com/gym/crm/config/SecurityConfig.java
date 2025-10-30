package com.gym.crm.config;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; 
import org.springframework.security.crypto.password.PasswordEncoder;   
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults; 

@Configuration
@EnableWebSecurity 
public class SecurityConfig {

    private final TraineeDAO traineeDAO;
    private final TrainerDAO trainerDAO;

    public SecurityConfig(TraineeDAO traineeDAO, TrainerDAO trainerDAO) {
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); 
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            return traineeDAO.findByUsername(username)
                    .<UserDetails>map(trainee -> User.builder()
                            .username(trainee.getUsername())
                            .password(trainee.getPassword())
                            .roles("TRAINEE") 
                            .build())
                    .orElseGet(() ->
                        trainerDAO.findByUsername(username)
                                .<UserDetails>map(trainer -> User.builder()
                                        .username(trainer.getUsername())
                                        .password(trainer.getPassword())
                                        .roles("TRAINER") 
                                        .build())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username))
                    );
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                                 "/",
                                 "/api/auth/**",
                                 "/api/trainees/register",
                                 "/api/trainers/register",
                                 "/swagger-ui.html",
                                 "/swagger-ui/**",
                                 "/v3/api-docs/**",
                                 "/h2-console/**",
                                 "/actuator/**" 
                ).permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults());

        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}