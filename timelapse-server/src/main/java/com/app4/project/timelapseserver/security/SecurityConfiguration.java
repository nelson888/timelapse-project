package com.app4.project.timelapseserver.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private UserDetailsService userDetailsService;
  @Autowired
  PasswordEncoder passwordEncoder;
  @Value("${jwt.secret}")
  private String jwtSecret;
  @Value("${jwt.expiration.time}")
  private long expirationTimeInMillis;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        // make sure we use stateless session; session won't be used to store user's state.
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        // handle an authorized attempts
        .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(
        HttpServletResponse.SC_UNAUTHORIZED))
        .and()
        // Add a filter to validate user credentials and add token in the response header

        // What's the authenticationManager()?
        // An object provided by WebSecurityConfigurerAdapter, used to authenticate the user passing user's credentials
        // The filter needs this auth manager to authenticate the user.
        .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), jwtSecret, expirationTimeInMillis))
        .authorizeRequests()
        // allow all POST requests
        .antMatchers(HttpMethod.POST, "/auth").permitAll()
        // any other requests must be authenticated
        .anyRequest().authenticated();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
  }
}
