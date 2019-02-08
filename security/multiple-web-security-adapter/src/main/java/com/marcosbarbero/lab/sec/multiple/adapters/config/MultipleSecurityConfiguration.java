package com.marcosbarbero.lab.sec.multiple.adapters.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static com.marcosbarbero.lab.sec.multiple.adapters.config.MultipleSecurityConfiguration.ApiSecurityConfiguration.ORDER;

@EnableWebSecurity
public class MultipleSecurityConfiguration {

    @Configuration
    @Order(ORDER)
    static
    class ApiSecurityConfiguration extends WebSecurityConfigurerAdapter {

        static final int ORDER = 1;

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
