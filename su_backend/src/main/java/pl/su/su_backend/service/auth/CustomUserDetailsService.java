package pl.su.su_backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.user.UsersRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Ładowanie użytkownika: {}", email);

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika o emailu: " + email));

        return new User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                user.isActive(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                !user.isBlocked(), // accountNonLocked
                getAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Users user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        if (user.getUserRoles() != null) {
            for (UserRole userRole : user.getUserRoles()) {
                String roleName = "ROLE_" + userRole.getRole().getRoleCode().name();
                authorities.add(new SimpleGrantedAuthority(roleName));

                if (userRole.getRole().getPermissions() != null) {
                    userRole.getRole().getPermissions().forEach(permission -> {
                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                    });
                }
            }
        }

        return authorities;
    }
}