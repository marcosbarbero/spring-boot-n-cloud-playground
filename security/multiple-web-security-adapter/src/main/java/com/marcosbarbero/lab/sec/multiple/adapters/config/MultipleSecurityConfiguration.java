package com.marcosbarbero.lab.sec.multiple.adapters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static com.marcosbarbero.lab.sec.multiple.adapters.config.MultipleSecurityConfiguration.ApiSecurityConfiguration.ORDER;

@EnableWebSecurity
public class MultipleSecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Configuration
    @Order(ORDER)
    static
    class ApiSecurityConfiguration extends WebSecurityConfigurerAdapter {

        static final int ORDER = 1;

        private final PasswordEncoder passwordEncoder;

        ApiSecurityConfiguration(final PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }

        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http
                    .antMatcher("/api/**")
                    .httpBasic()
                    .and()
                    .authorizeRequests().antMatchers("/api/**").authenticated();
        }

        @Override
        public void configure(final WebSecurity web) throws Exception {
            super.configure(web);
        }

        @Override
        protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userDetailsService())
                    .passwordEncoder(passwordEncoder);
        }

        @Bean
        @Override
        protected UserDetailsService userDetailsService() {
            UserDetails user = new User("user",
                    passwordEncoder.encode("pass"),
                    AuthorityUtils.createAuthorityList("USER"));
            return new InMemoryUserDetailsManager(user);
        }

        @Override
        @Bean("apiAuthenticationManagerBean")
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

    }

    @Configuration
    @Order(ORDER + 1)
    static
    class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http
                    .formLogin()
                    .and()
                    .authorizeRequests().antMatchers("/member/**").authenticated()
                    .and()
                    .authorizeRequests().antMatchers("/**").permitAll();
        }

        @Override
        public void configure(final WebSecurity web) {
            web.ignoring().antMatchers("/assets/**");
        }
    }

}
