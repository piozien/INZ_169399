package pl.su.su_backend.testsupport;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public final class CookieTestUtils {

    private CookieTestUtils() {
    }

    public static void assertHasCookie(ResponseEntity<?> response, String cookieName) {
        Assertions.assertNotNull(
            extractCookieValue(response, cookieName),
            () -> "Expected cookie %s to be present".formatted(cookieName)
        );
    }

    public static String extractCookieValue(ResponseEntity<?> response, String cookieName) {
        if (response.getHeaders().get(HttpHeaders.SET_COOKIE) == null) {
            return null;
        }
        return response.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
            .filter(cookie -> cookie.startsWith(cookieName + "="))
            .map(cookie -> {
                int endIdx = cookie.indexOf(';');
                if (endIdx == -1) {
                    endIdx = cookie.length();
                }
                return cookie.substring(cookieName.length() + 1, endIdx);
            })
            .findFirst()
            .orElse(null);
    }
}

