package pl.su.su_backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.user.UsersRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.getAuthProvider() == AuthProvider.MICROSOFT) {
            log.warn("User {} attempted local login but has Microsoft OAuth2 account", email);
            throw new UsernameNotFoundException("This account uses Microsoft login. Please use 'Login with Microsoft' button.");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(isUserEnabled(user))
                .build();
    }


    private Collection<? extends GrantedAuthority> getAuthorities(Users user) {
        return user.getUserRoles().stream()
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().
                        getRoleCode().name()))
                .collect(Collectors.toList());
    }

    private boolean isUserEnabled(Users user) {
        return user.getStatus() == null ||
                (!user.getStatus().name().equals("CONFIRMED") && !user.getStatus().name().equals("PENDING"));
    }
}
