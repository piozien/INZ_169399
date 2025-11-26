package pl.su.su_backend.controller.auth;

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

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto userRequestDto) {
        log.info("Registration request for: {}", userRequestDto.getEmail());
        UserResponseDto user = userService.registerLocalUser(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authenticationService.authenticateLocal(loginRequestDto));
    }

    @PostMapping("/microsoft")
    public ResponseEntity<LoginResponseDto> microsoftLogin(@Valid @RequestBody MicrosoftLoginDto request) {
        return ResponseEntity.ok(authenticationService.authenticateMicrosoft(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        log.info("User logged out (token removed on client side)");
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