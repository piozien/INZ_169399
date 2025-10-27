package pl.su.su_backend.controller.classCon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.classes.ClassesRequestDto;
import pl.su.su_backend.dto.classes.ClassesResponseDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.service.classServ.ClassesService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Slf4j
public class ClassesController {

    private final ClassesService classesService;

    @GetMapping
    public ResponseEntity<List<ClassesResponseDto>> list(@AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(classesService.list(principal.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassesResponseDto> get(@PathVariable UUID id, @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(classesService.get(id, principal.getUsername()));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CLASS_CREATE')")
    public ResponseEntity<ClassesResponseDto> create(@RequestBody ClassesRequestDto dto, @AuthenticationPrincipal User principal) {
        log.info("Creating class by user: {}", principal.getUsername());
        return ResponseEntity.ok(classesService.create(dto, principal.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'CLASS_EDIT')")
    public ResponseEntity<ClassesResponseDto> update(@PathVariable UUID id,
                                                     @RequestBody ClassesRequestDto dto,
                                                     @AuthenticationPrincipal User principal) {
        log.info("Updating class {} by user: {}", id, principal.getUsername());
        return ResponseEntity.ok(classesService.update(id, dto, principal.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'CLASS_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal User principal) {
        log.info("Deleting class {} by user: {}", id, principal.getUsername());
        classesService.delete(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("hasPermission(null, 'CLASS_VIEW')")
    public ResponseEntity<List<UserResponseDto>> users(@PathVariable UUID id, @AuthenticationPrincipal User principal) {
        log.info("Fetching users for class {} by user: {}", id, principal.getUsername());
        return ResponseEntity.ok(classesService.getUsers(id, principal.getUsername()));
    }

    @DeleteMapping("/users/{userId}/class")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<Void> detachUser(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Detaching user {} from class by user: {}", userId, principal.getUsername());
        classesService.detachUser(userId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}


