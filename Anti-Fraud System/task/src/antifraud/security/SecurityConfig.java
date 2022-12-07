package antifraud.security;

import antifraud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserService userService;

    @Autowired
    RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                .antMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll()
                .antMatchers(HttpMethod.GET, "/api/auth/list/**").hasAnyRole("ADMINISTRATOR","SUPPORT")
                .antMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasAnyRole("ADMINISTRATOR")
                .antMatchers(HttpMethod.POST, "/api/antifraud/transaction/**").hasAnyRole("MERCHANT")
                .antMatchers(HttpMethod.PUT, "/api/auth/access/**").hasAnyRole("ADMINISTRATOR")
                .antMatchers(HttpMethod.PUT, "/api/auth/role/**").hasAnyRole("ADMINISTRATOR")
                .antMatchers(HttpMethod.POST,"/api/antifraud/suspicious-ip/**").hasAnyRole("SUPPORT")
                .antMatchers(HttpMethod.DELETE,"/api/antifraud/suspicious-ip/**").hasAnyRole("SUPPORT")
                .antMatchers(HttpMethod.GET,"/api/antifraud/suspicious-ip/**").hasAnyRole("SUPPORT")
                .antMatchers(HttpMethod.POST,"/api/antifraud/stolencard/**").hasAnyRole("SUPPORT")
                .antMatchers(HttpMethod.DELETE,"/api/antifraud/stolencard/**").hasAnyRole("SUPPORT")
                .antMatchers(HttpMethod.GET,"/api/antifraud/stolencard/**").hasAnyRole("SUPPORT")
                .antMatchers(HttpMethod.GET,"/api/antifraud/history/**").hasAnyRole("SUPPORT")
                .antMatchers(HttpMethod.PUT, "/api/antifraud/transaction/**").hasAnyRole("SUPPORT")
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userService)
                .passwordEncoder(passwordEncoder());
    }
}
