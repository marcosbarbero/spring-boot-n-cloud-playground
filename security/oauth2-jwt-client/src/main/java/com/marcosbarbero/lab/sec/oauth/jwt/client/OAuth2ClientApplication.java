package com.marcosbarbero.lab.sec.oauth.jwt.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@EnableOAuth2Sso
@SpringBootApplication
public class OAuth2ClientApplication {

    public static void main(String... args) {
        SpringApplication.run(OAuth2ClientApplication.class, args);
    }


    @RestController
    class Controller {

        private final OAuth2RestTemplate oAuth2RestTemplate;

        Controller(OAuth2RestTemplate oAuth2RestTemplate) {
            this.oAuth2RestTemplate = oAuth2RestTemplate;
        }

        @GetMapping("/avengers")
        public ResponseEntity<Collection<String>> get() {
            ResponseEntity<Collection<String>> response = oAuth2RestTemplate.exchange("http://localhost:9100/avengers",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Collection<String>>() {
                    });
            return ResponseEntity.ok(response.getBody());
        }
    }

}
