package xenophan.microservice.authservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import xenophan.microservice.authservice.entity.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class UserDTO implements UserDetails {
    private final User userObject;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.userObject.getRoles().stream().map(authority -> new SimpleGrantedAuthority(authority)).collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return userObject.getPassword();
    }

    @Override
    public String getUsername() {
        return userObject.getMail();
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
