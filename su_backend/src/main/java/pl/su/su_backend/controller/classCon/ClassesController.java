package pl.su.su_backend.controller.classCon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.classes.ClassesRequestDto;
import pl.su.su_backend.dto.classes.ClassesResponseDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.service.auth.AuthenticationService;
import pl.su.su_backend.service.classServ.ClassesService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Slf4j
public class ClassesController {

    private final ClassesService classesService;
    private final AuthenticationService authenticationService;

    @GetMapping
    public ResponseEntity<List<ClassesResponseDto>> list(@AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        return ResponseEntity.ok(classesService.list(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassesResponseDto> get(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        return ResponseEntity.ok(classesService.get(id, email));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CLASS_CREATE')")
    public ResponseEntity<ClassesResponseDto> create(@RequestBody ClassesRequestDto dto, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Creating class by user: {}", email);
        return ResponseEntity.ok(classesService.create(dto, email));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'CLASS_EDIT')")
    public ResponseEntity<ClassesResponseDto> update(@PathVariable UUID id,
                                                     @RequestBody ClassesRequestDto dto,
                                                     @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Updating class {} by user: {}", id, email);
        return ResponseEntity.ok(classesService.update(id, dto, email));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'CLASS_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Deleting class {} by user: {}", id, email);
        classesService.delete(id, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("hasPermission(null, 'CLASS_VIEW')")
    public ResponseEntity<List<UserResponseDto>> users(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching users for class {} by user: {}", id, email);
        return ResponseEntity.ok(classesService.getUsers(id, email));
    }

    @DeleteMapping("/users/{userId}/class")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<Void> detachUser(@PathVariable UUID userId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Detaching user {} from class by user: {}", userId, email);
        classesService.detachUser(userId, email);
        return ResponseEntity.noContent().build();
    }
}


