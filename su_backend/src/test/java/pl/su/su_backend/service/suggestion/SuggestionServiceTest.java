package pl.su.su_backend.service.suggestion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.su.su_backend.dto.suggestion.SuggestionRequestDto;
import pl.su.su_backend.dto.suggestion.SuggestionResponseDto;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.SuggestionStatus;
import pl.su.su_backend.model.suggestion.Suggestion;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.suggestion.SuggestionRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.testsupport.Fixtures;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
public class SuggestionServiceTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private SuggestionRepository suggestionRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private SuggestionService suggestionService;

    private Users testUser;
    private SuggestionRequestDto testSuggestionRequestDto;
    private Suggestion savedSuggestion;


    @BeforeEach
    void setUp() {
        testUser = Fixtures.user("Test User", "test@test.com");
        testUser.setId(UUID.randomUUID());

        testSuggestionRequestDto = Fixtures.suggestionRequestDto(testUser.getId(), "Test Suggestion",
                "Test description", false);

        savedSuggestion = Fixtures.suggestion(testUser, "Test Suggestion", "Test description",
                SuggestionStatus.PENDING);
        savedSuggestion.setId(UUID.randomUUID());
    }

    @Test
    void createSuggestion_ShouldCreateSuggestionSuccessfully_WhenValidData() {
        // Given
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

        // When
        SuggestionResponseDto result = suggestionService.createSuggestion(testSuggestionRequestDto, testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(savedSuggestion.getId(), result.getId());
        assertEquals(testSuggestionRequestDto.getTitle(), result.getTitle());
        assertEquals(testSuggestionRequestDto.getDescription(), result.getDescription());
        verify(usersRepository).findById(testUser.getId());
        verify(suggestionRepository).save(any(Suggestion.class));
    }

    @Test
    void createSuggestion_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () ->
                suggestionService.createSuggestion(testSuggestionRequestDto, testUser.getId()));
        verify(usersRepository).findById(testUser.getId());
    }

    @Test
    void getAllSuggestions_ShouldReturnAllSuggestions_WhenHasPermission() {
        // Given
        Suggestion suggestion1 = Fixtures.suggestion(testUser, "Suggestion 1", "Description 1",
                SuggestionStatus.PENDING);
        Suggestion suggestion2 = Fixtures.suggestion(testUser, "Suggestion 2", "Description 2",
                SuggestionStatus.APPROVED);

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW)).thenReturn(true);
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW_ANONYMOUS)).thenReturn(false);
        when(suggestionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(suggestion1, suggestion2));

        // When
        List<SuggestionResponseDto> result = suggestionService.getAllSuggestions(testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW);
    }

    @Test
    void getAllSuggestions_ShouldHideUserIdForAnonymousSuggestions_WhenUserHasNoPermission() {
        // Given
        Users anonymousUser = Fixtures.user("Anonymous User", "anon@test.com");
        anonymousUser.setId(UUID.randomUUID());

        Suggestion anonymousSuggestion = Fixtures.suggestion(anonymousUser, "Anonymous Suggestion", "Anonymous description",
                SuggestionStatus.PENDING);
        anonymousSuggestion.setId(UUID.randomUUID());
        anonymousSuggestion.setIsAnonymous(true);

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW)).thenReturn(true);
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW_ANONYMOUS)).thenReturn(false);
        when(suggestionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(anonymousSuggestion));

        // When
        List<SuggestionResponseDto> result = suggestionService.getAllSuggestions(testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.getFirst().getUserId());
        assertTrue(result.getFirst().getIsAnonymous());
    }

    @Test
    void getAllSuggestions_ShouldShowUserIdForAnonymousSuggestions_WhenUserHasPermission() {
        // Given
        Users anonymousUser = Fixtures.user("Anonymous User", "anon@test.com");
        anonymousUser.setId(UUID.randomUUID());

        Suggestion anonymousSuggestion = Fixtures.suggestion(anonymousUser, "Anonymous Suggestion",
                "Anonymous description", SuggestionStatus.PENDING);
        anonymousSuggestion.setId(UUID.randomUUID());
        anonymousSuggestion.setIsAnonymous(true);

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW)).thenReturn(true);
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW_ANONYMOUS)).thenReturn(true);
        when(suggestionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(anonymousSuggestion));

        // When
        List<SuggestionResponseDto> result = suggestionService.getAllSuggestions(testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(anonymousUser.getId(), result.getFirst().getUserId()); // userId should be visible for users with permission
        assertTrue(result.getFirst().getIsAnonymous());
    }

    @Test
    void getAllSuggestions_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () ->
                suggestionService.getAllSuggestions(testUser.getEmail()));
        verify(usersRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void getAllSuggestions_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW)).thenReturn(false);

        // When & Then
        assertThrows(ApiException.class, () ->
                suggestionService.getAllSuggestions(testUser.getEmail()));
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW);
    }

    @Test
    void getUserSuggestions_ShouldReturnUserSuggestions_WhenValidUserId() {
        // Given
        Suggestion userSuggestion = Fixtures.suggestion(testUser, "User Suggestion", "User description",
                SuggestionStatus.PENDING);

        when(suggestionRepository.findByUser_IdOrderByCreatedAtDesc(testUser.getId())).thenReturn(List.of(userSuggestion));

        // When
        List<SuggestionResponseDto> result = suggestionService.getUserSuggestions(testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userSuggestion.getId(), result.getFirst().getId());
        verify(suggestionRepository).findByUser_IdOrderByCreatedAtDesc(testUser.getId());
    }

    @Test
    void getSuggestionById_ShouldReturnSuggestion_WhenExistsAndHasPermission() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW)).thenReturn(true);
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW_ANONYMOUS)).thenReturn(false);
        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));

        // When
        SuggestionResponseDto result = suggestionService.getSuggestionById(savedSuggestion.getId(), testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(savedSuggestion.getId(), result.getId());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW);
        verify(suggestionRepository).findById(savedSuggestion.getId());
    }

    @Test
    void getSuggestionById_ShouldThrowException_WhenSuggestionNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW)).thenReturn(true);
        when(suggestionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () ->
                suggestionService.getSuggestionById(nonExistentId, testUser.getEmail()));
        verify(suggestionRepository).findById(nonExistentId);
    }

    @Test
    void getSuggestionById_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW)).thenReturn(false);

        // When & Then
        assertThrows(ApiException.class, () ->
                suggestionService.getSuggestionById(savedSuggestion.getId(), testUser.getEmail()));
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.SUGGESTION_VIEW);
    }

    @Test
    void approveSuggestion_ShouldApproveSuggestionSuccessfully_WhenValidData() {
        // Given
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_APPROVE)).thenReturn(true);
        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

        // When
        SuggestionResponseDto result = suggestionService.approveSuggestion(savedSuggestion.getId(), testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(SuggestionStatus.APPROVED, savedSuggestion.getStatus());
        assertNull(savedSuggestion.getRejectionReason());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.SUGGESTION_APPROVE);
        verify(suggestionRepository).findById(savedSuggestion.getId());
        verify(suggestionRepository).save(savedSuggestion);
    }

    @Test
    void approveSuggestion_ShouldThrowException_WhenNoPermission() {
        // Given
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_APPROVE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                suggestionService.approveSuggestion(savedSuggestion.getId(), testUser.getId()));
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.SUGGESTION_APPROVE);
    }

    @Test
    void approveSuggestion_ShouldThrowException_WhenSuggestionNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_APPROVE)).thenReturn(true);
        when(suggestionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () ->
                suggestionService.approveSuggestion(nonExistentId, testUser.getId()));
        verify(suggestionRepository).findById(nonExistentId);
    }

    @Test
    void approveSuggestion_ShouldThrowException_WhenSuggestionNotPending() {
        // Given
        savedSuggestion.setStatus(SuggestionStatus.APPROVED);
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_APPROVE)).thenReturn(true);
        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));

        // When & Then
        assertThrows(ApiException.class, () ->
                suggestionService.approveSuggestion(savedSuggestion.getId(), testUser.getId()));
        verify(suggestionRepository).findById(savedSuggestion.getId());
    }

    @Test
    void rejectSuggestion_ShouldRejectSuggestionSuccessfully_WhenValidData() {
        // Given
        String rejectionReason = "Test";
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_REJECT)).thenReturn(true);
        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

        // When
        SuggestionResponseDto result = suggestionService.rejectSuggestion(savedSuggestion.getId(), rejectionReason, testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(SuggestionStatus.REJECTED, savedSuggestion.getStatus());
        assertEquals(rejectionReason, savedSuggestion.getRejectionReason());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.SUGGESTION_REJECT);
        verify(suggestionRepository).findById(savedSuggestion.getId());
        verify(suggestionRepository).save(savedSuggestion);
    }

    @Test
    void rejectSuggestion_ShouldThrowException_WhenNoPermission() {
        // Given
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_REJECT)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                suggestionService.rejectSuggestion(savedSuggestion.getId(), "Reason", testUser.getId()));
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.SUGGESTION_REJECT);
    }

    @Test
    void rejectSuggestion_ShouldThrowException_WhenRejectionReasonEmpty() {
        // Given
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_REJECT)).thenReturn(true);
        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));

        // When & Then
        assertThrows(ApiException.class, () ->
                suggestionService.rejectSuggestion(savedSuggestion.getId(), "", testUser.getId()));
        verify(suggestionRepository).findById(savedSuggestion.getId());
    }

    @Test
    void rejectSuggestion_ShouldThrowException_WhenSuggestionNotPending() {
        // Given
        savedSuggestion.setStatus(SuggestionStatus.APPROVED);
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.SUGGESTION_REJECT)).thenReturn(true);
        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));

        // When & Then
        assertThrows(ApiException.class, () ->
                suggestionService.rejectSuggestion(savedSuggestion.getId(), "Reason", testUser.getId()));
        verify(suggestionRepository).findById(savedSuggestion.getId());
    }

    @Test
    void updateSuggestion_ShouldUpdateSuggestionSuccessfully_WhenValidData() {
        // Given
        SuggestionRequestDto updateDto = Fixtures.suggestionRequestDto(testUser.getId(), "Updated Title",
                "Updated description", true);
        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

        // When
        SuggestionResponseDto result = suggestionService.updateSuggestion(savedSuggestion.getId(), updateDto, testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(updateDto.getTitle(), savedSuggestion.getTitle());
        assertEquals(updateDto.getDescription(), savedSuggestion.getDescription());
        assertEquals(updateDto.getIsAnonymous(), savedSuggestion.getIsAnonymous());
        verify(suggestionRepository).findById(savedSuggestion.getId());
        verify(suggestionRepository).save(savedSuggestion);
    }

    @Test
    void updateSuggestion_ShouldThrowException_WhenNotOwnerAndNoPermission() {
        // Given
        Users otherUser = Fixtures.user("Other User", "other@test.com");
        otherUser.setId(UUID.randomUUID());
        SuggestionRequestDto updateDto = Fixtures.suggestionRequestDto(testUser.getId(), "Updated Title",
                "Updated description", true);

        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));
        when(permissionService.hasPermission(otherUser.getId(), PermissionCode.SUGGESTION_EDIT)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                suggestionService.updateSuggestion(savedSuggestion.getId(), updateDto, otherUser.getId()));
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(suggestionRepository).findById(savedSuggestion.getId());
        verify(permissionService).hasPermission(otherUser.getId(), PermissionCode.SUGGESTION_EDIT);
    }

    @Test
    void updateSuggestion_ShouldUpdateSuggestionSuccessfully_WhenNotOwnerButHasPermission() {
        // Given
        Users otherUser = Fixtures.user("Other User", "other@test.com");
        otherUser.setId(UUID.randomUUID());
        SuggestionRequestDto updateDto = Fixtures.suggestionRequestDto(testUser.getId(), "Updated Title",
                "Updated description", true);

        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));
        when(permissionService.hasPermission(otherUser.getId(), PermissionCode.SUGGESTION_EDIT)).thenReturn(true);
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

        // When
        SuggestionResponseDto result = suggestionService.updateSuggestion(savedSuggestion.getId(), updateDto, otherUser.getId());

        // Then
        assertNotNull(result);
        verify(permissionService).hasPermission(otherUser.getId(), PermissionCode.SUGGESTION_EDIT);
        verify(suggestionRepository).save(savedSuggestion);
    }

    @Test
    void updateSuggestion_ShouldThrowException_WhenSuggestionNotPending() {
        // Given
        savedSuggestion.setStatus(SuggestionStatus.APPROVED);
        SuggestionRequestDto updateDto = Fixtures.suggestionRequestDto(testUser.getId(), "Updated Title",
                "Updated description", true);

        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));

        // When & Then
        assertThrows(ApiException.class, () ->
                suggestionService.updateSuggestion(savedSuggestion.getId(), updateDto, testUser.getId()));
        verify(suggestionRepository).findById(savedSuggestion.getId());
    }

    @Test
    void deleteSuggestion_ShouldDeleteSuggestionSuccessfully_WhenValidData() {
        // Given
        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));

        // When
        suggestionService.deleteSuggestion(savedSuggestion.getId(), testUser.getId());

        // Then
        verify(suggestionRepository).findById(savedSuggestion.getId());
        verify(suggestionRepository).delete(savedSuggestion);
    }

    @Test
    void deleteSuggestion_ShouldDeleteSuggestionSuccessfully_WhenNotOwnerButHasPermission() {
        // Given
        Users otherUser = Fixtures.user("Other User", "other@test.com");
        otherUser.setId(UUID.randomUUID());

        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));
        when(permissionService.hasPermission(otherUser.getId(), PermissionCode.SUGGESTION_DELETE)).thenReturn(true);

        // When
        suggestionService.deleteSuggestion(savedSuggestion.getId(), otherUser.getId());

        // Then
        verify(permissionService).hasPermission(otherUser.getId(), PermissionCode.SUGGESTION_DELETE);
        verify(suggestionRepository).findById(savedSuggestion.getId());
        verify(suggestionRepository).delete(savedSuggestion);
    }

    @Test
    void deleteSuggestion_ShouldThrowException_WhenNotOwnerAndNoPermission() {
        // Given
        Users otherUser = Fixtures.user("Other User", "other@test.com");
        otherUser.setId(UUID.randomUUID());

        when(suggestionRepository.findById(savedSuggestion.getId())).thenReturn(Optional.of(savedSuggestion));
        when(permissionService.hasPermission(otherUser.getId(), PermissionCode.SUGGESTION_DELETE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
                suggestionService.deleteSuggestion(savedSuggestion.getId(), otherUser.getId()));
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(suggestionRepository).findById(savedSuggestion.getId());
        verify(permissionService).hasPermission(otherUser.getId(), PermissionCode.SUGGESTION_DELETE);
    }

    @Test
    void deleteSuggestion_ShouldThrowException_WhenSuggestionNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(suggestionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () ->
                suggestionService.deleteSuggestion(nonExistentId, testUser.getId()));
        verify(suggestionRepository).findById(nonExistentId);
    }
}
