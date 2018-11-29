package com.app4.project.timelapseserver.security;

import com.app4.project.timelapse.model.Roles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
@Order(100)
public class TokenConfiguration extends WebSecurityConfigurerAdapter {


  @Value("${jwt.secret}")
  private String jwtSecret;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable() // Disable CSRF (cross site request forgery)
        // No user session is needed
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        // handle an authorized attempts
        .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(
        HttpServletResponse.SC_UNAUTHORIZED))
        .and()
        // Add a filter to validate the tokens with every request
        .addFilterAfter(new JwtAuthFilter(jwtSecret), UsernamePasswordAuthenticationFilter.class)
        // authorization requests config
        .authorizeRequests()
        // Any other request must be authenticated
        .anyRequest().authenticated()
        //TODO add other matchers ? or in the other?
        .antMatchers(HttpMethod.PUT, "api/commands/new").hasAnyAuthority(Roles.ANDROID, Roles.ADMIN);
  }

}
