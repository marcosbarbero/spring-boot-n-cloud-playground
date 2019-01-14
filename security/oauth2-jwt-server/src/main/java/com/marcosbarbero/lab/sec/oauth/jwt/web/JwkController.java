package com.marcosbarbero.lab.sec.oauth.jwt.web;

import com.marcosbarbero.lab.sec.oauth.jwt.config.props.SecurityProperties;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
public class JwkController {

    private final SecurityProperties securityProperties;

    public JwkController(final SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @GetMapping("/jwk/public")
    public ResponseEntity<Jwk> jwk() {
        return ResponseEntity.ok(getJwk());
    }

    private Jwk getJwk() {
        try {
            return new Jwk(IOUtils.toString(securityProperties.getJwt().getPublicKey().getInputStream(), UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class Jwk {
        private String value;

        public Jwk(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
