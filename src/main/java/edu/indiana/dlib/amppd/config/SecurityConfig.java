package edu.indiana.dlib.amppd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity 
public class SecurityConfig extends WebSecurityConfigurerAdapter {  

	  @Override
	  protected void configure(HttpSecurity http) throws Exception {
	    http
	      .authorizeRequests()
	        .antMatchers("/", "/home").permitAll()
	        .anyRequest().authenticated()
	        .and()
	      .formLogin()
	        .loginPage("/login")
	        .permitAll()
	        .and()
	      .logout()
	        .permitAll();
	  }

	  @Bean
	  @Override
	  public UserDetailsService userDetailsService() {
	    UserDetails user =
	       User.withDefaultPasswordEncoder()
	        .username("user")
	        .password("password")
	        .roles("USER")
	        .build();

	    return new InMemoryUserDetailsManager(user);
	  }


//	@Override
//	protected void configure(HttpSecurity http) throws Exception 
//	{
//		http.authorizeRequests().antMatchers("/").permitAll().and().authorizeRequests().antMatchers("/console/**").permitAll();
//		http.authorizeRequests()
//	    	.anyRequest()
//	        .permitAll()
//	        .and().csrf().disable();
//		http.headers().frameOptions().disable();
//	}

}
