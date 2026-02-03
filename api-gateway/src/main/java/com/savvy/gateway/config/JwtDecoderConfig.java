package com.savvy.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

@Configuration
public class JwtDecoderConfig {

    private static final Pattern HEX = Pattern.compile("^[0-9a-fA-F]+$");

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(JwtProps props) {
        byte[] keyBytes = decodeSecret(props.getSignerKey());
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        return NimbusReactiveJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private byte[] decodeSecret(String s) {
        if (s == null) throw new IllegalArgumentException("jwt.signerKey is null");
        String t = s.trim();

        if (t.length() % 2 == 0 && HEX.matcher(t).matches()) {
            byte[] out = new byte[t.length() / 2];
            for (int i = 0; i < out.length; i++) {
                int hi = Character.digit(t.charAt(i * 2), 16);
                int lo = Character.digit(t.charAt(i * 2 + 1), 16);
                out[i] = (byte) ((hi << 4) + lo);
            }
            return out;
        }
        return Base64.getDecoder().decode(t);
    }
}
