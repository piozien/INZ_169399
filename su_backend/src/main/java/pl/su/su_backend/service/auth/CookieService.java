package pl.su.su_backend.service.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.su.su_backend.config.JwtConfig;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieService {

    private final JwtConfig jwtConfig;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String sameSite;
    
    //https://sekurak.pl/flaga-cookies-samesite-jak-dziala-i-przed-czym-zapewnia-ochrone
    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtConfig.getJwtExpiration() / 1000));
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtConfig.getRefreshExpiration() / 1000));
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }

    public void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        log.info("Setting auth cookies - Secure: {}, SameSite: {}, Path: /", cookieSecure, sameSite);
        log.info("Frontend URL context - cookies will be set for cross-origin request");
        setAccessTokenCookie(response, accessToken);
        setRefreshTokenCookie(response, refreshToken);
        log.info("Auth cookies set successfully - accessToken and refreshToken");
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }

    public void clearAuthCookies(HttpServletResponse response) {
        clearAccessTokenCookie(response);
        clearRefreshTokenCookie(response);
    }
}

