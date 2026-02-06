package com.savvycom.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class JwtAuthConverterConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(this::authoritiesFromClaims);
        return c;
    }

    private Collection<GrantedAuthority> authoritiesFromClaims(Jwt jwt) {
        Set<GrantedAuthority> out = new LinkedHashSet<>();

        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            for (String r : roles) {
                if (r != null && !r.isBlank()) out.add(new SimpleGrantedAuthority("ROLE_" + r.trim()));
            }
        }

        List<String> perms = jwt.getClaimAsStringList("permissions");
        if (perms != null) {
            for (String p : perms) {
                if (p != null && !p.isBlank()) out.add(new SimpleGrantedAuthority(p.trim()));
            }
        }

        return out;
    }
}