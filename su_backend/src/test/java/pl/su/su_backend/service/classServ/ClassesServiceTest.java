package pl.su.su_backend.service.classServ;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.su.su_backend.dto.classes.ClassesRequestDto;
import pl.su.su_backend.dto.classes.ClassesResponseDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.classRep.ClassesRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.testsupport.Fixtures;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassesServiceTest {

    @Mock
    private ClassesRepository classesRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private ClassesService classesService;

    private Users testUser;
    private ClassesRequestDto testRequestDto;
    private Classes savedClass;

    @BeforeEach
    void setUp() {
        testUser = Fixtures.user("Test User", "test@test.com");
        testUser.setId(UUID.randomUUID());

        testRequestDto = Fixtures.classesRequestDto("1A", "2025");

        savedClass = Fixtures.schoolClass("1A", "2025");
        savedClass.setId(UUID.randomUUID());
    }

    @Test
    void create_ShouldCreateClassSuccessfully_WhenValidData() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_CREATE)).thenReturn(true);
        when(classesRepository.findByNameAndYear(testRequestDto.getName(), testRequestDto.getYear())).thenReturn(Optional.empty());
        when(classesRepository.save(any(Classes.class))).thenReturn(savedClass);

        // When
        ClassesResponseDto result = classesService.create(testRequestDto, testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(savedClass.getId(), result.getId());
        assertEquals(savedClass.getName(), result.getName());
        assertEquals(savedClass.getYear(), result.getYear());

        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_CREATE);
        verify(classesRepository).findByNameAndYear(testRequestDto.getName(), testRequestDto.getYear());
        verify(classesRepository).save(any(Classes.class));
    }

    @Test
    void create_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
                () -> classesService.create(testRequestDto, testUser.getEmail()));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getCode());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verifyNoInteractions(permissionService);
        verifyNoInteractions(classesRepository);
    }

    @Test
    void create_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_CREATE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
                () -> classesService.create(testRequestDto, testUser.getEmail()));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_CREATE);
        verifyNoInteractions(classesRepository);
    }

    @Test
    void create_ShouldThrowException_WhenClassAlreadyExists() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_CREATE)).thenReturn(true);
        when(classesRepository.findByNameAndYear(testRequestDto.getName(), testRequestDto.getYear())).thenReturn(Optional.of(savedClass));

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
                () -> classesService.create(testRequestDto, testUser.getEmail()));

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getCode());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_CREATE);
        verify(classesRepository).findByNameAndYear(testRequestDto.getName(), testRequestDto.getYear());
        verify(classesRepository, never()).save(any(Classes.class));
    }

    @Test
    void list_ShouldReturnAllClasses_WhenHasPermission() {
        // Given
        Classes class1 = Fixtures.schoolClass("1A", "2024");
        class1.setId(UUID.randomUUID());
        Classes class2 = Fixtures.schoolClass("2B", "2024");
        class2.setId(UUID.randomUUID());
        List<Classes> classes = List.of(class1, class2);

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW)).thenReturn(true);
        when(classesRepository.findAll()).thenReturn(classes);

        // When
        List<ClassesResponseDto> result = classesService.list(testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        ClassesResponseDto result1 = result.getFirst();
        assertEquals(class1.getId(), result1.getId());
        assertEquals(class1.getName(), result1.getName());
        assertEquals(class1.getYear(), result1.getYear());

        ClassesResponseDto result2 = result.get(1);
        assertEquals(class2.getId(), result2.getId());
        assertEquals(class2.getName(), result2.getName());
        assertEquals(class2.getYear(), result2.getYear());

        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW);
        verify(classesRepository).findAll();
    }

    @Test
    void list_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
                () -> classesService.list(testUser.getEmail()));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getCode());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verifyNoInteractions(permissionService);
        verifyNoInteractions(classesRepository);
    }

    @Test
    void list_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
                () -> classesService.list(testUser.getEmail()));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW);
        verifyNoInteractions(classesRepository);
    }

    @Test
    void get_ShouldReturnClass_WhenExistsAndHasPermission() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW)).thenReturn(true);
        when(classesRepository.findById(savedClass.getId())).thenReturn(Optional.of(savedClass));

        // When & THen
        ClassesResponseDto result = classesService.get(savedClass.getId(), testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(savedClass.getId(), result.getId());
        assertEquals(savedClass.getName(), result.getName());
        assertEquals(savedClass.getYear(), result.getYear());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW);
        verify(classesRepository).findById(savedClass.getId());
    }

    @Test
    void get_ShouldThrowException_WhenClassNotFound() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW)).thenReturn(true);
        when(classesRepository.findById(savedClass.getId())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
                () -> classesService.get(savedClass.getId(), testUser.getEmail()));

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getCode());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW);
        verify(classesRepository).findById(savedClass.getId());
    }

    @Test
    void get_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
                () -> classesService.get(savedClass.getId(), testUser.getEmail()));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW);
        verifyNoInteractions(classesRepository);
    }

    @Test
    void update_ShouldUpdateClassSuccessfully_WhenValidData() {
        // Given
        ClassesRequestDto updateDTO = Fixtures.classesRequestDto("Updated name", "updated year");
        Classes updatedClass = Fixtures.schoolClass("Updated name", "updated year");
        updatedClass.setId(savedClass.getId());

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_EDIT)).thenReturn(true);
        when(classesRepository.findById(savedClass.getId())).thenReturn(Optional.of(savedClass));
        when(classesRepository.findByNameAndYear(updateDTO.getName(), updateDTO.getYear())).thenReturn(Optional.of(savedClass));
        when(classesRepository.save(any(Classes.class))).thenReturn(updatedClass);

        // When
        ClassesResponseDto result = classesService.update(savedClass.getId(), updateDTO, testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(updatedClass.getId(), result.getId());
        assertEquals(updatedClass.getName(), result.getName());
        assertEquals(updatedClass.getYear(), result.getYear());

        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_EDIT);
        verify(classesRepository).findById(savedClass.getId());
        verify(classesRepository).save(any(Classes.class));
    }

    @Test
    void update_ShouldThrowException_WhenUserNotFound() {
        // Given
        ClassesRequestDto updateDTO = Fixtures.classesRequestDto("Updated name", "updated year");
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
                () -> classesService.update(savedClass.getId(), updateDTO, testUser.getEmail()));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getCode());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verifyNoInteractions(permissionService);
        verifyNoInteractions(classesRepository);
    }

    @Test
    void update_ShouldThrowException_WhenNoPermission() {
        // given
        ClassesRequestDto updateDTO = Fixtures.classesRequestDto("Updated name", "updated year");
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_EDIT)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
                () -> classesService.update(savedClass.getId(), updateDTO, testUser.getEmail()));
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_EDIT);
        verifyNoInteractions(classesRepository);

    }

    @Test
    void delete_ShouldDeleteClassSuccessfully_WhenHasValidData() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_DELETE)).thenReturn(true);
        when(classesRepository.findById(savedClass.getId())).thenReturn(Optional.of(savedClass));

        // When
        classesService.delete(savedClass.getId(), testUser.getEmail());

        // then
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_DELETE);
        verify(classesRepository).findById(savedClass.getId());
        verify(classesRepository).deleteById(savedClass.getId());
    }

    @Test
    void getUsers_ShouldReturnClassUsers_WhenHasPermission() {
        // Givne
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW)).thenReturn(true);
        when(classesRepository.findById(savedClass.getId())).thenReturn(Optional.of(savedClass));

        // When
        List<UserResponseDto> result = classesService.getUsers(savedClass.getId(), testUser.getEmail());

        //Then
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_VIEW);
        verify(classesRepository).findById(savedClass.getId());
        verify(usersRepository).findByClasses_Id(savedClass.getId());
        assertNotNull(result);
    }

    @Test
    void detachUser_ShouldDetachUserFromClassSuccessfully_WhenHasValidData() {
        // Given
        Users detachUser = Fixtures.user("Detach User", "det@example.com");
        detachUser.setId(UUID.randomUUID());
        detachUser.setClasses(savedClass);
        
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_EDIT)).thenReturn(true);
        when(usersRepository.findById(detachUser.getId())).thenReturn(Optional.of(detachUser));
        when(usersRepository.save(any(Users.class))).thenReturn(detachUser);

        // When
        classesService.detachUser(detachUser.getId(), testUser.getEmail());

        // Then
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_EDIT);
        verify(usersRepository).findById(detachUser.getId());
        verify(usersRepository).save(detachUser);
        assertNull(detachUser.getClasses());
    }
}