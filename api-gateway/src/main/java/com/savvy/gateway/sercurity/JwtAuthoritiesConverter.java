// v2

package com.savvy.gateway.sercurity;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String rolesClaim;
    private final String permissionsClaim;

    public JwtAuthoritiesConverter() {
        this("roles", "permissions");
    }

    public JwtAuthoritiesConverter(String rolesClaim, String permissionsClaim) {
        this.rolesClaim = rolesClaim;
        this.permissionsClaim = permissionsClaim;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> out = new LinkedHashSet<>();

        List<String> roles = readStringList(jwt.getClaim(rolesClaim));
        for (String r : roles) {
            if (r == null || r.isBlank()) continue;
            out.add(new SimpleGrantedAuthority("ROLE_" + r.trim()));
        }

        List<String> perms = readStringList(jwt.getClaim(permissionsClaim));
        for (String p : perms) {
            if (p == null || p.isBlank()) continue;
            out.add(new SimpleGrantedAuthority(p.trim()));
        }

        return out;
    }

    private List<String> readStringList(Object claimVal) {
        if (claimVal == null) return List.of();

        if (claimVal instanceof Collection<?> c) {
            if (CollectionUtils.isEmpty(c)) return List.of();
            List<String> out = new ArrayList<>();
            for (Object o : c) out.add(String.valueOf(o));
            return out;
        }

        return List.of(String.valueOf(claimVal));
    }
}
