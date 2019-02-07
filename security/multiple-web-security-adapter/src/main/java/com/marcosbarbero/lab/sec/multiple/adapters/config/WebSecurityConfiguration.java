package com.marcosbarbero.lab.sec.multiple.adapters.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(ApiSecurityConfiguration.ORDER + 1)
class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

}
