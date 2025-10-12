package pl.su.su_backend.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.user.UsersRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final UsersRepository usersRepository;

    public void saveRefreshToken(UUID userId, String refreshToken) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
         user.setRefreshToken(refreshToken);
         usersRepository.save(user);
    }

    public void revokeRefreshToken(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRefreshToken(null);
        usersRepository.save(user);
    }

    public boolean isRefreshTokenValid(UUID userId, String refreshToken) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return refreshToken != null && refreshToken.equals(user.getRefreshToken());
    }
}
