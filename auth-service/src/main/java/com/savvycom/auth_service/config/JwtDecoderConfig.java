package com.savvycom.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

@Configuration
public class JwtDecoderConfig {

    private static final Pattern HEX = Pattern.compile("^[0-9a-fA-F]+$");

    @Bean
    public JwtDecoder jwtDecoder(@Value("${application.security.jwt.secret-key}") String secret) {
        byte[] keyBytes = decodeSecret(secret);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private byte[] decodeSecret(String s) {
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
