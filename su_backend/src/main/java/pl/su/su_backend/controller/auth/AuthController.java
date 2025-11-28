package pl.su.su_backend.controller.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pl.su.su_backend.dto.auth.LoginRequestDto;
import pl.su.su_backend.dto.auth.LoginResponseDto;
import pl.su.su_backend.dto.auth.MicrosoftLoginDto;
import pl.su.su_backend.dto.auth.RefreshTokenRequestDto;
import pl.su.su_backend.dto.auth.ResendActivationRequestDto;
import pl.su.su_backend.dto.user.UserRequestDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.AuthenticationService;
import pl.su.su_backend.service.auth.CookieService;
import pl.su.su_backend.service.auth.JwtService;
import pl.su.su_backend.service.user.MailService;
import pl.su.su_backend.service.user.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final MailService mailService;
    private final CookieService cookieService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto userRequestDto) {
        log.info("Registration request for: {}", userRequestDto.getEmail());
        UserResponseDto user = userService.registerLocalUser(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
       LoginResponseDto loginResponse = authenticationService.authenticateLocal(loginRequestDto);
       cookieService.setAuthCookies(response, loginResponse.getAccessToken(), loginResponse.getRefreshToken());

       return ResponseEntity.ok(loginResponse.getUser());
    }

    @PostMapping("/microsoft")
    public ResponseEntity<UserResponseDto> microsoftLogin(@Valid @RequestBody MicrosoftLoginDto request, HttpServletResponse response) {
        LoginResponseDto loginResponse = authenticationService.authenticateMicrosoft(request);
        cookieService.setAuthCookies(response, loginResponse.getAccessToken(), loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse.getUser());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            throw ApiException.unauthorized("Brak tokenu odświeżającego w ciasteczkach");
        }

        LoginResponseDto newTokens = authenticationService.refreshToken(refreshToken);
        cookieService.setAuthCookies(response, newTokens.getAccessToken(), newTokens.getRefreshToken());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieService.clearAuthCookies(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activate(@RequestParam("token") String token) {
        if (!jwtService.isActivationToken(token)) {
            throw ApiException.badRequest("Nieprawidłowy link aktywacyjny");
        }

        String email = jwtService.extractEmail(token);
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("Użytkownik nie istnieje"));

        if (user.getStatus() != StatusEnum.CONFIRMED) {
            user.setStatus(StatusEnum.CONFIRMED);
            usersRepository.save(user);
            log.info("User {} activated successfully", email);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/activate/resend")
    public ResponseEntity<Void> resendActivation(@Valid @RequestBody ResendActivationRequestDto requestDto) {
        String email = requestDto.getEmail();

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("Użytkownik nie istnieje"));

        if (user.getStatus() == StatusEnum.CONFIRMED) {
            return ResponseEntity.ok().build();
        }

        String activationToken = jwtService.generateActivationToken(user.getEmail());

        String activationUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/activate")
                .queryParam("token", activationToken)
                .build()
                .toUriString();

        mailService.sendActivationEmail(user.getEmail(), user.getFullName(), activationUrl);

        return ResponseEntity.ok().build();
    }
}