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

#### Database schema

For the sake of this tutorial we'll be using [H2 Database](http://www.h2database.com/html/main.html).  
Here you can find a reference SQL schema required by Spring Security.

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

>Note: As this tutorial uses `JWT` not all the tables are required.

#### Java Code

Now we need to do some Java code, as you might be familiar we need a startup class for a Spring Boot application:

```java
package com.marcosbarbero.lab.sec.oauth.jwt;

import com.marcosbarbero.lab.sec.oauth.jwt.config.props.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class OAuth2ServerJwtApplication {

    public static void main(String... args) {
        SpringApplication.run(OAuth2ServerJwtApplication.class, args);
    }

}
```

### Resource Server
 
# Footnote
 - The code used for this tutorial can be found on [GitHub](https://github.com/marcosbarbero/spring-boot-n-cloud-playground/tree/master/security).
 - [OAuth 2.0](https://www.oauth.com/) 