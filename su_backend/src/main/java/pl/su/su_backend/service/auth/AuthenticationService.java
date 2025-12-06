package pl.su.su_backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import pl.su.su_backend.dto.auth.LoginRequestDto;
import pl.su.su_backend.dto.auth.LoginResponseDto;
import pl.su.su_backend.dto.auth.MicrosoftLoginDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.user.UserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final UserService userService;
    private final RestClient graphRestClient;

    public LoginResponseDto authenticateLocal(LoginRequestDto request) {
        log.info("Local login: {}", request.getEmail());
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            Users user = usersRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> ApiException.notFound("Użytkownik nie istnieje"));

            if (user.isBlocked()) {
                log.warn("A blocked user is attempting to log in: {}", user.getEmail());
                throw ApiException.forbidden("Twoje konto zostało zablokowane. Skontaktuj się z administratorem.");
            }

            if (user.getStatus() == StatusEnum.PENDING) {
                log.warn("An inactive user is attempting to log in.: {}", user.getEmail());
                throw ApiException.forbidden("Konto nie zostało jeszcze aktywowane. Sprawdź skrzynkę email.");
            }

            return generateResponse(user);
        } catch (Exception e) {
            log.warn("Local login error: {}", e.getMessage());
            throw ApiException.unauthorized( "Błędny email lub hasło");
        }
    }

    @Transactional
    public LoginResponseDto authenticateMicrosoft(MicrosoftLoginDto request) {
        log.info("Microsoft token verification...");

        try {
            Map msUser = graphRestClient.get()
                    .uri("/me")
                    .header("Authorization", "Bearer " + request.getToken())
                    .retrieve()
                    .body(Map.class);

            log.info("Odpowiedź z Microsoft Graph: {}", msUser);

            if (msUser == null) throw ApiException.unauthorized( "Nieprawidłowy token");

            String email = (String) msUser.getOrDefault("mail", msUser.get("userPrincipalName"));
            String externalId = (String) msUser.get("id");
            String displayName = (String) msUser.get("displayName");

            if (email == null) throw ApiException.unauthorized( "Brak emaila w koncie MS");

            Users user = userService.getOrCreateMicrosoftUser(email, displayName, externalId);

            if (user.isBlocked()) {
                throw ApiException.forbidden("Konto zablokowane");
            }

            return generateResponse(user);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error MS: {}", e.getMessage());
            throw ApiException.unauthorized( "Błąd weryfikacji konta Microsoft");
        }
    }

    private LoginResponseDto generateResponse(Users user) {
        String accessToken = jwtService.generateToken(user.getEmail(), user.getFullName());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        long expiresIn = jwtService.getJwtExpiration();

        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getRoleCode().name())
                .collect(Collectors.toList());

        UserResponseDto userDto = UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .build();

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .user(userDto)
                .roles(roles)
                .build();
    }

    public String getEmailFromPrincipal(Object principal) {
        return switch (principal) {
            case UserDetails user -> user.getUsername();
            case String email -> email;
            case null -> null;
            default -> throw new IllegalArgumentException("Nie rozpoznano Użytkownika");
        };
    }

    public LoginResponseDto refreshToken(String refreshToken) {
        String email;
        try {
            email = jwtService.extractEmail(refreshToken);
        } catch (Exception e) {
            throw ApiException.unauthorized("Nieprawidłowy token odświeżający");
        }

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.unauthorized("Użytkownik nie istnieje"));

        if (!jwtService.isTokenValid(refreshToken, email)) {
            throw ApiException.unauthorized("Token odświeżający wygasł");
        }
        return generateResponse(user);
    }
}