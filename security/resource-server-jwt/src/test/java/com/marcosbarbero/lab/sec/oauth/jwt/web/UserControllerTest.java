package com.marcosbarbero.lab.sec.oauth.jwt.web;

import com.marcosbarbero.lab.sec.oauth.jwt.ds.web.UserController;
import com.marcosbarbero.lab.test.context.security.oauth2.jwt.WithJwtToken;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserController.class)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithJwtToken(tokenProducerMethod = "createToken")
    @Ignore
    public void shouldReturnAuthentication() throws Exception {
        this.mockMvc.perform(get("/me")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello World")));
    }

    private static Jwt createToken() {
        return createToken(null);
    }

    private static Jwt createTokenWithScopes() {
        return createToken(Arrays.asList("one", "two"));
    }

    private static Jwt createToken(@Nullable List<String> scopes) {
        try {
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plusSeconds(120L)))
                    .issuer("https://github.com/spring-projects/spring-security")
                    .claim("user", "user")
                    .subject("user");

            Optional.ofNullable(scopes)
                    .ifPresent(theScopes -> claimsBuilder.claim("scp", theScopes));

            PlainJWT jwt = new PlainJWT(claimsBuilder.build());

            return new Jwt(
                    jwt.serialize(),
                    jwt.getJWTClaimsSet().getIssueTime().toInstant(),
                    jwt.getJWTClaimsSet().getExpirationTime().toInstant(),
                    jwt.getHeader().toJSONObject(),
                    jwt.getJWTClaimsSet().getClaims()
            );
        }
        catch (ParseException ex) {
            return ExceptionUtils.rethrow(ex);
        }
    }
}