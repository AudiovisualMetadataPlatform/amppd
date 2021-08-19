package edu.indiana.dlib.amppd.authentication.unused;

//TODO make this work

//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class ServerSecurityConfig {//extends WebSecurityConfigurerAdapter {
//
//   private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
//
//   private final UserDetailsService userDetailsService;
//
//   public ServerSecurityConfig(CustomAuthenticationEntryPoint customAuthenticationEntryPoint, @Qualifier("userService")
//           UserDetailsService userDetailsService) {
//       this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
//       this.userDetailsService = userDetailsService;
//   }
//
//   @Bean
//   public DaoAuthenticationProvider authenticationProvider() {
//       DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//       provider.setPasswordEncoder(passwordEncoder());
//       provider.setUserDetailsService(userDetailsService);
//       return provider;
//   }
//
//   @Bean
//   public PasswordEncoder passwordEncoder() {
//       return new BCryptPasswordEncoder();
//   }
//
//   @Bean
//   @Override
//   public AuthenticationManager authenticationManagerBean() throws Exception {
//       return super.authenticationManagerBean();
//   }
//
//   @Override
//   protected void configure(HttpSecurity http) throws Exception {
//       http
//               .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//               .and()
//               .authorizeRequests()
//               .antMatchers("/login/**").permitAll()
//               .antMatchers("/api/**").hasAnyAuthority("ADMIN", "USER")
//               .antMatchers("/api/users/**").hasAuthority("ADMIN")
//               .antMatchers("/api/**").authenticated()
//               .anyRequest().authenticated()
//               .and().exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint).accessDeniedHandler(new CustomAccessDeniedHandler());
//   }

}