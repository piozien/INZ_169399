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

    public static ApiException conflict(ErrorCode code, String msg) {
        return new ApiException(code, HttpStatus.CONFLICT, msg);
    }

    public static ApiException unauthorized(ErrorCode code, String msg) {
        return new ApiException(code, HttpStatus.UNAUTHORIZED, msg);
    }

    public static ApiException forbidden(ErrorCode code, String msg) {
        return new ApiException(code, HttpStatus.FORBIDDEN, msg);
    }

    public static ApiException badRequest(ErrorCode code, String msg) {
        return new ApiException(code, HttpStatus.BAD_REQUEST, msg);
    }

}


