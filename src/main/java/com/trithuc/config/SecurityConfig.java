package com.trithuc.config;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JWTRequesFilter jwtrequesFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // .exceptionHandling().authenticationEntryPoint(userAuthenticationEntryPoint)
                // .and()
                .csrf(csrf -> csrf.disable())
                //	.headers().frameOptions().disable()
                //	.and()
				.sessionManagement(httpSecurityManagementConfigure -> {
                    httpSecurityManagementConfigure
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
				})
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/",
                                "public",
                                "api,/files/**",
                                "api/cart/delete/**",
                                "/api/upload",
                                "/api/**",
                                "/api/posts",
                                "/api/auth/profile",
                                "/registration/**",
                                "/api/registration/**",
                                "/login",
                                "/403",
                                "/css",
                                "/images/**").permitAll()
                        //.requestMatchers("/admin").hasRole("ADMIN")
                        //.requestMatchers("/user").hasRole("USER")
                        //.requestMatchers("/business").hasRole("BUSINESS")
                        .requestMatchers("/registration", "/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.GET).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtrequesFilter, UsernamePasswordAuthenticationFilter
                        .class);
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//	@Bean
//	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenconfig) throws Exception {
//		final List<GlobalAuthenticationConfigurerAdapter> configurers = new ArrayList<>();
//		configurers.add(new GlobalAuthenticationConfigurerAdapter() {
//
//			@Override
//		public void configure(AuthenticationManagerBuilder auth) throws Exception {
//				auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
//			}
//		});
//		return authenconfig.getAuthenticationManager();
//	}

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails adminDetails = User.builder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();
        UserDetails user = User.builder()
                .username("User")
                .password("password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(adminDetails, user);
    }


}
