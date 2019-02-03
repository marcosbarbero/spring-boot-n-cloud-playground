Password Encoder Migration with Spring Security 5
---

Recently I was working in a project that used a custom `PasswordEncoder` and there was a requirement to migrate it to 
[bcrypt](https://en.wikipedia.org/wiki/Bcrypt). The current passwords are stored all as `hash` which means it's not
possible to revert it to the original `String` - at least not in an easy way.

The challenge here was how to support both implementations, the old hash solution along with the new `bcrypt` 
implementation. After a little research I could find [Spring Security 5's](https://spring.io/projects/spring-security) 
`DelegatingPasswordEncoder`.

## Meet DelegatingPasswordEncoder

The `DelegatingPasswordEncoder` class makes it possible to support multiple `password encoders` based on a *prefix*. The
password is stored like this:

```text
{bcrypt}$2a$10$vCXMWCn7fDZWOcLnIEhmK.74dvK1Eh8ae2WrWlhr2ETPLoxQctN4.
{noop}plaintextpassword
``` 

[Spring Security 5](https://spring.io/projects/spring-security) brings the handy `PasswordEncoderFactories.createDelegatingPasswordEncoder()`,
currently this class supports the following encoders:

```java
public static PasswordEncoder createDelegatingPasswordEncoder() {
	String encodingId = "bcrypt";
	Map<String, PasswordEncoder> encoders = new HashMap<>();
	encoders.put(encodingId, new BCryptPasswordEncoder());
	encoders.put("ldap", new org.springframework.security.crypto.password.LdapShaPasswordEncoder());
	encoders.put("MD4", new org.springframework.security.crypto.password.Md4PasswordEncoder());
	encoders.put("MD5", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
	encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
	encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
	encoders.put("scrypt", new SCryptPasswordEncoder());
	encoders.put("SHA-1", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-1"));
	encoders.put("SHA-256", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256"));
	encoders.put("sha256", new org.springframework.security.crypto.password.StandardPasswordEncoder());

	return new DelegatingPasswordEncoder(encodingId, encoders);
}
``` 

Now instead of declaring a single `PasswordEncoder` we can use the `PasswordEncoderFactories`, like this snippet of code:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

## Adding a Custom Encoder

Now, getting back to my initial problem, for legacy reasons there is a homegrown `password encoding` solution, and the
handy `PasswordEncoderFactories` knows nothing about it, to solve that I've created a class similar to the 
`PasswordEncoderFactories` and I've added all the built-in encoders along with my custom one, here's a sample implementation:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

class DefaultPasswordEncoderFactories {

    @SuppressWarnings("deprecation")
    static PasswordEncoder createDelegatingPasswordEncoder() {
        String encodingId = "bcrypt";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(encodingId, new BCryptPasswordEncoder());
        encoders.put("ldap", new org.springframework.security.crypto.password.LdapShaPasswordEncoder());
        encoders.put("MD4", new org.springframework.security.crypto.password.Md4PasswordEncoder());
        encoders.put("MD5", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
        encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
        encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
        encoders.put("scrypt", new SCryptPasswordEncoder());
        encoders.put("SHA-1", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-1"));
        encoders.put("SHA-256", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256"));
        encoders.put("sha256", new org.springframework.security.crypto.password.StandardPasswordEncoder());
        encoders.put("custom", new CustomPasswordEncoder());

        return new DelegatingPasswordEncoder(encodingId, encoders);
    }
}
```

And then I declared my `@Bean` using the `DefaultPasswordEncoderFactories` instead.

After my first run I realized another problem, I would have to run a `SQL` script to update all the existing passwords 
adding the `{custom}` prefix so the framework could properly bind the prefix with the right `PasswordEncoder`, 
don't get me wrong it's an fine solution but I really did not want to mess around with existing passwords in the 
database and luckily for us the `DelegatingPasswordEncoder` class allows us to set a *default* `PasswordEncoder`, it means
whenever the framework tries doesn't find a prefix in the stored password it will fallback to the `default` one to try
to decode it.

Then I changed my implementation to the following:

```java

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

class DefaultPasswordEncoderFactories {

    @SuppressWarnings("deprecation")
    static PasswordEncoder createDelegatingPasswordEncoder() {
        String encodingId = "bcrypt";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(encodingId, new BCryptPasswordEncoder());
        encoders.put("ldap", new org.springframework.security.crypto.password.LdapShaPasswordEncoder());
        encoders.put("MD4", new org.springframework.security.crypto.password.Md4PasswordEncoder());
        encoders.put("MD5", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
        encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
        encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
        encoders.put("scrypt", new SCryptPasswordEncoder());
        encoders.put("SHA-1", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-1"));
        encoders.put("SHA-256", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256"));

        DelegatingPasswordEncoder delegatingPasswordEncoder = new DelegatingPasswordEncoder(encodingId, encoders);
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new CustomPasswordEncoder());
        return delegatingPasswordEncoder;
    }

}
```

And the `@Bean` declaration is now:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return DefaultPasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

Conclusion
---

Migration password encoders is a real life problem and [Spring Security 5](https://spring.io/projects/spring-security) 
gives a quite handy way to easily handle it.

Footnote
---
 - The code used for this tutorial can be found on [GitHub](https://github.com/marcosbarbero/spring-boot-n-cloud-playground/tree/master/security).
 - [DelegatingPasswordEncoder - Spring Docs](https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#pe-dpe)