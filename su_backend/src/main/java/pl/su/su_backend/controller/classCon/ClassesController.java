package pl.su.su_backend.controller.classCon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.classes.ClassesRequestDto;
import pl.su.su_backend.dto.classes.ClassesResponseDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.service.classServ.ClassesService;
import pl.su.su_backend.repositories.user.UsersRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClassesController {

    private final ClassesService classesService;
    private final UsersRepository usersRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClassesResponseDto>> list(@AuthenticationPrincipal User principal) {
        boolean isStudentOnly = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_UCZEN") || a.equals("ROLE_BYLY_UCZEN"));

        if (isStudentOnly) {
            var current = usersRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            if (current.getClasses() == null) return ResponseEntity.ok(List.of());
            return ResponseEntity.ok(List.of(classesService.get(current.getClasses().getId(), principal.getUsername())));
        }

        return ResponseEntity.ok(classesService.list(principal.getUsername()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClassesResponseDto> get(@PathVariable UUID id, @AuthenticationPrincipal User principal) {
        boolean isStudentOnly = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_UCZEN") || a.equals("ROLE_BYLY_UCZEN"));
        if (isStudentOnly) {
            var current = usersRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            if (current.getClasses() == null || !id.equals(current.getClasses().getId())) {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.ok(classesService.get(id, principal.getUsername()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClassesResponseDto> create(@RequestBody ClassesRequestDto dto, @AuthenticationPrincipal User principal) {
        log.info("Creating class by user: {}", principal.getUsername());
        try {
            return ResponseEntity.ok(classesService.create(dto, principal.getUsername()));
        } catch (Exception e) {
            log.error("Failed to create class: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClassesResponseDto> update(@PathVariable UUID id,
                                                     @RequestBody ClassesRequestDto dto,
                                                     @AuthenticationPrincipal User principal) {
        log.info("Updating class {} by user: {}", id, principal.getUsername());
        try {
            return ResponseEntity.ok(classesService.update(id, dto, principal.getUsername()));
        } catch (Exception e) {
            log.error("Failed to update class {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal User principal) {
        log.info("Deleting class {} by user: {}", id, principal.getUsername());
        try {
            classesService.delete(id, principal.getUsername());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete class {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponseDto>> users(@PathVariable UUID id, @AuthenticationPrincipal User principal) {
        log.info("Fetching users for class {} by user: {}", id, principal.getUsername());
        try {
            return ResponseEntity.ok(classesService.getUsers(id, principal.getUsername()));
        } catch (Exception e) {
            log.error("Failed to fetch users for class {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/users/{userId}/class")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> detachUser(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Detaching user {} from class by user: {}", userId, principal.getUsername());
        try {
            classesService.detachUser(userId, principal.getUsername());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to detach user {} from class: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}


