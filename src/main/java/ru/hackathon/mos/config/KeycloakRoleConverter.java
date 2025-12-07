package ru.hackathon.mos.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @SuppressWarnings("unchecked")
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // клиентские роли
        Map<String, Object> resourceAccess = (Map<String, Object>) jwt.getClaims().get("resource_access");
        if (resourceAccess != null) {
            resourceAccess.forEach((client, value) -> {
                Map<String, Object> clientRoles = (Map<String, Object>) value;
                if (clientRoles.get("roles") instanceof List<?> rolesList) {
                    rolesList.forEach(role -> authorities.add(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    ));
                }
            });
        }

        // realm-роли
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> realmRoles) {
            realmRoles.forEach(role -> authorities.add(
                    new SimpleGrantedAuthority("ROLE_" + role)
            ));
        }

        return authorities;
    }
}