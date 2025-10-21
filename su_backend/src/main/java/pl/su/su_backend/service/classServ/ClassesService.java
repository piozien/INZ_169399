package pl.su.su_backend.service.classServ;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.users.Users;

import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.dto.classes.ClassesRequestDto;
import pl.su.su_backend.dto.classes.ClassesResponseDto;
import pl.su.su_backend.dto.classes.ClassesMapper;
import pl.su.su_backend.dto.user.UserMapper;
import pl.su.su_backend.repositories.classRep.ClassesRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.model.enums.PermissionCode;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassesService {

    private final ClassesRepository classesRepository;
    private final UsersRepository usersRepository;
    private final PermissionService permissionService;

    public ClassesResponseDto create(ClassesRequestDto dto, String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.CLASS_CREATE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        classesRepository.findByName(dto.getName()).ifPresent(c -> {
            throw ApiException.conflict(ErrorCode.VALIDATION_ERROR, "Class already exists");
        });
        Classes c = ClassesMapper.toEntity(dto);
        return ClassesMapper.toResponse(classesRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<ClassesResponseDto> list(String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.CLASS_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        return classesRepository.findAll().stream().map(ClassesMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClassesResponseDto get(UUID id, String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.CLASS_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Classes c = classesRepository.findById(id).orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Class not found"));
        return ClassesMapper.toResponse(c);
    }

    public ClassesResponseDto update(UUID id, ClassesRequestDto dto, String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.CLASS_EDIT)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Classes c = classesRepository.findById(id).orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Class not found"));
        c.setName(dto.getName());
        c.setYear(dto.getYear());
        return ClassesMapper.toResponse(classesRepository.save(c));
    }

    public void delete(UUID id, String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.CLASS_DELETE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        classesRepository.findById(id)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Class not found"));

        classesRepository.deleteById(id);

    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsers(UUID classId, String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.CLASS_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        if (!classesRepository.findById(classId).isPresent()) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Class not found");
        }
        
        return usersRepository.findByClasses_Id(classId).stream()
                .map(UserMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public void detachUser(UUID userId, String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.CLASS_EDIT)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Users user = usersRepository.findById(userId).orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        user.setClasses(null);
        usersRepository.save(user);
    }

}


