package ru.berkut.spring.hubflow.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.berkut.spring.hubflow.entity.User;
import ru.berkut.spring.hubflow.enums.SystemRole;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Обёртка над User для Spring Security.
 * Глобальных ролей нет — роли на уровне когорты, поэтому authorities пустые.
 * Проверка прав происходит в сервисах через CohortMembership.
 */

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID       id;
    private final String     email;
    private final String     password;
    private final SystemRole systemRole;
    private final boolean    active;

    public UserPrincipal(User user) {
        this.id         = user.getId();
        this.email      = user.getEmail();
        this.password   = user.getPasswordHash();
        this.systemRole = user.getSystemRole();
        this.active     = Boolean.TRUE.equals(user.getIsActive());
    }

    public boolean isAdmin()  { return systemRole == SystemRole.ADMIN; }
    public boolean isMentor() { return systemRole == SystemRole.MENTOR; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ROLE_ADMIN, ROLE_MENTOR, ROLE_USER — для @PreAuthorize
        return List.of(new SimpleGrantedAuthority("ROLE_" + systemRole.name()));
    }

    @Override public String  getUsername()              { return email; }
    @Override public boolean isAccountNonExpired()      { return true; }
    @Override public boolean isAccountNonLocked()       { return true; }
    @Override public boolean isCredentialsNonExpired()  { return true; }
    @Override public boolean isEnabled()                { return active; }
}
