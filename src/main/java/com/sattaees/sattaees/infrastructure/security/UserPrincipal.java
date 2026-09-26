package com.sattaees.sattaees.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails implementation holding authenticated identity,
 * user ID, email, and roles for access control and ownership verification.
 */
@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(Long id, String email, String password, String role) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        return UserPrincipal.builder()
                .id(id)
                .email(email)
                .password(password)
                .role(role.toUpperCase())
                .authorities(authorities)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
