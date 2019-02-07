package com.marcosbarbero.lab.sec.multiple.adapters.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static com.marcosbarbero.lab.sec.multiple.adapters.config.ApiSecurityConfiguration.ORDER;

@Configuration
@Order(ORDER)
class ApiSecurityConfiguration extends WebSecurityConfigurerAdapter {

    static final int ORDER = 1;

}
