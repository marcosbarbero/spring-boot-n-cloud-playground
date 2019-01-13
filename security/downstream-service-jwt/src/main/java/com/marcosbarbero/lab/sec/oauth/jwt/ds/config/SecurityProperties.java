package com.marcosbarbero.lab.sec.oauth.jwt.ds.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import javax.validation.constraints.NotNull;

@ConfigurationProperties("security")
public class SecurityProperties {

    private JwtProperties jwt;

    public JwtProperties getJwt() {
        return jwt;
    }

    public void setJwt(JwtProperties jwt) {
        this.jwt = jwt;
    }

    public static class JwtProperties {

        @NotNull
        private Resource publicKey;

        public Resource getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(Resource publicKey) {
            this.publicKey = publicKey;
        }
    }

}
