package pl.su.su_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorCode code;
    private final HttpStatus status;

    private ApiException(ErrorCode code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    // --- 400 BAD REQUEST ---
    public static ApiException badRequest(String msg) {
        return new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, msg);
    }

    public static ApiException badRequest(ErrorCode code, String msg) {
        return new ApiException(code, HttpStatus.BAD_REQUEST, msg);
    }

    // --- 401 UNAUTHORIZED ---
    public static ApiException unauthorized(String msg) {
        return new ApiException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, msg);
    }

    public static ApiException unauthorized(ErrorCode code, String msg) {
        return new ApiException(code, HttpStatus.UNAUTHORIZED, msg);
    }

    // --- 403 FORBIDDEN ---
    public static ApiException forbidden(String msg) {
        return new ApiException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, msg);
    }

    public static ApiException forbidden(ErrorCode code, String msg) {
        return new ApiException(code, HttpStatus.FORBIDDEN, msg);
    }

    // --- 404 NOT FOUND ---
    public static ApiException notFound(String msg) {
        return new ApiException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, msg);
    }

    public static ApiException notFound(ErrorCode code, String msg) {
        return new ApiException(code, HttpStatus.NOT_FOUND, msg);
    }

    // --- 409 CONFLICT ---
    public static ApiException conflict(String msg) {
        return new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, msg);
    }

    public static ApiException conflict(ErrorCode code, String msg) {
        return new ApiException(code, HttpStatus.CONFLICT, msg);
    }
}