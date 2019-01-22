Centralized Authentication and Authorization with JWT using Spring Boot 2
---

This guide walks through the process to create a centralized authentication and authorization server with Spring Boot 2, 
a demo resource server will also be provided.

>If you're not familiar with OAuth2 I recommend this [read](https://www.oauth.com/).

## Pre-req
 
 - [JDK 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 - Text editor or your favorite IDE
 - [Maven 3.0+](https://maven.apache.org/download.cgi)

## Implementation Overview

For this project we'll be using [Spring Security 5](https://spring.io/projects/spring-security) through Spring Boot.
If you're familiar with the earlier versions this [Spring Boot Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Migration-Guide#oauth2)
might be useful.

## OAuth2 Terminology

  - **Resource Owner**
    - The user who authorizes an application to access his account. The access is limited to the `scope`.
  - **Resource Server**:
    -  A server that handles authenticated requests after the `client` has obtained an `access token`.
  - **Client**
    - An application that access protected resources on behalf of the resource owner.
  - **Authorization Server**
    - A server which issues access tokens after successfully authenticating a `client` and `resource owner`, and authorizing the request.
  - **Access Token**
    - A unique token used to access protected resources
  - **Scope**
    - A Permission
  - **JWT**
    - JSON Web Token is a method for representing claims securely between two parties as defined in [RFC 7519](https://tools.ietf.org/html/rfc7519)
  - **Grant type**
    - A `grant` is a method of acquiring an access token. 
    - [Read more about grant types here](https://oauth.net/2/grant-types/)

### Authorization Server

To build our `Auth Server` we'll be using [Spring Security 5.x](https://spring.io/projects/spring-security) through 
[Spring Boot 2.0.x](https://spring.io/projects/spring-boot).

#### Dependencies

You can go to [start.spring.io](https://start.spring.io/) and generate a new project and then add the following dependencies:

```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
		
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security.oauth.boot</groupId>
            <artifactId>spring-security-oauth2-autoconfigure</artifactId>
            <version>2.1.2.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>		
    </dependencies>
```

#### Database

For the sake of this tutorial we'll be using [H2 Database](http://www.h2database.com/html/main.html).  
Here you can find a reference OAuth2 SQL schema required by Spring Security.

```sql
CREATE TABLE IF NOT EXISTS oauth_client_details (
  client_id VARCHAR(256) PRIMARY KEY,
  resource_ids VARCHAR(256),
  client_secret VARCHAR(256) NOT NULL,
  scope VARCHAR(256),
  authorized_grant_types VARCHAR(256),
  web_server_redirect_uri VARCHAR(256),
  authorities VARCHAR(256),
  access_token_validity INTEGER,
  refresh_token_validity INTEGER,
  additional_information VARCHAR(4000),
  autoapprove VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS oauth_client_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication_id VARCHAR(256) PRIMARY KEY,
  user_name VARCHAR(256),
  client_id VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS oauth_access_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication_id VARCHAR(256),
  user_name VARCHAR(256),
  client_id VARCHAR(256),
  authentication BLOB,
  refresh_token VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS oauth_refresh_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication BLOB
);

CREATE TABLE IF NOT EXISTS oauth_code (
  code VARCHAR(256), authentication BLOB
);
```

And then add the following entry

```sql
-- The encrypted client_secret it `secret`
INSERT INTO oauth_client_details (client_id, client_secret, scope, authorized_grant_types, authorities, access_token_validity)
  VALUES ('clientId', '{bcrypt}$2a$10$vCXMWCn7fDZWOcLnIEhmK.74dvK1Eh8ae2WrWlhr2ETPLoxQctN4.', 'read,write', 'password,refresh_token,client_credentials', 'ROLE_CLIENT', 300);
```

>The `client_secret` above was generated using [bcrypt](https://en.wikipedia.org/wiki/Bcrypt).  
>The suffix `{bcrypt}` is required because we'll using Spring Security 5.x's new feature of [DelegatingPasswordEncoder](https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#pe-dpe).

Bellow here you can find a User/Authority reference SQL schema used by Spring's `org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl`.

```sql
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(256) NOT NULL,
  password VARCHAR(256) NOT NULL,
  enabled TINYINT(1),
  UNIQUE KEY unique_username(username)
);

CREATE TABLE IF NOT EXISTS authorities (
  username VARCHAR(256) NOT NULL,
  authority VARCHAR(256) NOT NULL,
  PRIMARY KEY(username, authority)
);
```

Same as before add the following entries for the user and its authority.

```sql
-- The encrypted password is `pass`
INSERT INTO users (id, username, password, enabled) VALUES (1, 'user', '{bcrypt}$2a$10$cyf5NfobcruKQ8XGjUJkEegr9ZWFqaea6vjpXWEaSqTa2xL9wjgQC', 1);
INSERT INTO authorities (username, authority) VALUES ('user', 'ROLE_USER');
```

>Note: As this tutorial uses `JWT` not all the tables are required.

#### Spring Security Configuration

Add the following java configuration.

```java
package com.marcosbarbero.lab.sec.oauth.jwt.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;

    private PasswordEncoder passwordEncoder;
    private UserDetailsService userDetailsService;

    public WebSecurityConfiguration(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        if (passwordEncoder == null) {
            passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
        return passwordEncoder;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        if (userDetailsService == null) {
            userDetailsService = new JdbcDaoImpl();
            ((JdbcDaoImpl) userDetailsService).setDataSource(dataSource);
        }
        return userDetailsService;
    }

}
```

Quoting from [Spring Blog](https://spring.io/blog/2013/07/03/spring-security-java-config-preview-web-security#websecurityconfigureradapter):

>The @EnableWebSecurity annotation and WebSecurityConfigurerAdapter work together to provide web based security.

If you are using Spring Boot the `DataSource` object will be auto-configured and you can just inject it to the class instead of defining it yourself.
it needs to be injected to the `UserDetailsService` in which will be using the provided `JdbcDaoImpl` provided by Spring Security, if necessary 
you can replace this with your own implementation.

As the Spring Security's `AuthenticationManager` is required by some auto-configured Spring `@Bean`s it's necessary to
override the `authenticationManagerBean` method and annotate is as a `@Bean`.

The `PasswordEncoder` will be handled by `PasswordEncoderFactories.createDelegatingPasswordEncoder()` in which handles a
few of password encoders and delegates based on a prefix, in our example we are prefixing the passwords with `{bcrypt}`.

### Resource Server
 
# Footnote
 - The code used for this tutorial can be found on [GitHub](https://github.com/marcosbarbero/spring-boot-n-cloud-playground/tree/master/security).
 - [OAuth 2.0](https://www.oauth.com/) 
 - [Spring Security Java Config Preview](https://spring.io/blog/2013/07/03/spring-security-java-config-preview-web-security)
 - [Spring Boot 2 - Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Migration-Guide#authenticationmanager-bean)