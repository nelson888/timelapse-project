package com.app4.project.timelapseserver.configuration;

import com.app4.project.timelapseserver.repository.LocalUserRepository;
import com.app4.project.timelapseserver.repository.UserRepository;
import com.app4.project.timelapseserver.security.JwtConfigurer;
import com.app4.project.timelapseserver.security.JwtTokenProvider;
import com.app4.project.timelapseserver.security.Role;
import com.app4.project.timelapseserver.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {


  @Value("${security.jwt.token.secret-key:secret}")
  private String secretKey = "secret";

  @Autowired
  JwtTokenProvider jwtTokenProvider;

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  public String secretKey() {
    return Base64.getEncoder().encodeToString(secretKey.getBytes());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public List<UserDetailsImpl> users(PasswordEncoder passwordEncoder) {
    return Arrays.asList(
      new UserDetailsImpl("android", passwordEncoder.encode("fdshsdfmhlhdfs"), Role.ANDROID),
      new UserDetailsImpl("timelapse", passwordEncoder.encode("mlijmbstrhlz"), Role.TIMELAPSE),
      new UserDetailsImpl("admin", passwordEncoder.encode("gsfhsghsdfdshq"), Role.ADMIN));
  }

  @Bean
  public UserRepository userRepository(List<UserDetailsImpl> users) {
    return new LocalUserRepository(users);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    final String android = Role.ANDROID.name();
    final String timelapse = Role.TIMELAPSE.name();
    final String admin = Role.ADMIN.name();
    http
      .httpBasic().disable()
      .csrf().disable()
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()
      .antMatchers("/auth/signin").permitAll()
      //CommandController
      .antMatchers(HttpMethod.POST, "/api/commands/").hasAnyRole(android, admin)
      .antMatchers(HttpMethod.GET, "/api/commands/consume").hasAnyRole(admin, timelapse)
      //ExecutionController
      .antMatchers(HttpMethod.POST, "/api/executions/").hasAnyRole(android, admin)
      .antMatchers(HttpMethod.GET, "/api/executions/").authenticated()
      .antMatchers(HttpMethod.DELETE, "/api/executions/**").hasAnyRole(android, admin)
      .antMatchers(HttpMethod.PUT, "/api/executions/**").hasAnyRole(android, admin)
      .antMatchers(HttpMethod.GET, "/api/executions/count").authenticated()
      .antMatchers(HttpMethod.GET, "/api/executions/soonest").authenticated()
      .antMatchers(HttpMethod.GET, "/api/executions/current").authenticated()
      .antMatchers(HttpMethod.GET, "/api/executions/").authenticated()
      //StateController
      .antMatchers(HttpMethod.GET, "/api/state").authenticated()
      .antMatchers(HttpMethod.PUT, "/api/state").hasAnyRole(timelapse, admin)
      .antMatchers(HttpMethod.GET, "/api/globalState").hasRole(android)
      //StorageController
      .antMatchers(HttpMethod.PUT, "/storage/**").hasAnyRole(timelapse, admin)
      .antMatchers(HttpMethod.GET, "/storage/**").hasAnyRole(android, admin)

      .anyRequest().authenticated()
      .and()
      .apply(new JwtConfigurer(jwtTokenProvider));
  }

}
