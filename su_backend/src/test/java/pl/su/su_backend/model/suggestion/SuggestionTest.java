package pl.su.su_backend.model.suggestion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.enums.SuggestionStatus;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.testsupport.Fixtures;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SuggestionTest {

    @Test
    void builderSetsAllFields() {
        UUID id = UUID.randomUUID();
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        String title = "Test Suggestion";
        String description = "Test description";
        Boolean isAnonymous = false;
        SuggestionStatus status = SuggestionStatus.PENDING;
        String rejectionReason = null;
        LocalDateTime createdAt = LocalDateTime.now();
        Set<SuggestionTag> tags = new HashSet<>();

        Suggestion suggestion = Suggestion.builder()
                .id(id)
                .user(user)
                .title(title)
                .description(description)
                .isAnonymous(isAnonymous)
                .status(status)
                .rejectionReason(rejectionReason)
                .createdAt(createdAt)
                .tags(tags)
                .build();

        Assertions.assertEquals(id, suggestion.getId());
        Assertions.assertEquals(user, suggestion.getUser());
        Assertions.assertEquals(title, suggestion.getTitle());
        Assertions.assertEquals(description, suggestion.getDescription());
        Assertions.assertEquals(isAnonymous, suggestion.getIsAnonymous());
        Assertions.assertEquals(status, suggestion.getStatus());
        Assertions.assertEquals(rejectionReason, suggestion.getRejectionReason());
        Assertions.assertEquals(createdAt, suggestion.getCreatedAt());
        Assertions.assertEquals(tags, suggestion.getTags());
    }

    @Test
    void canChangeFieldsViaSetters() {
        Suggestion suggestion = new Suggestion();
        UUID id = UUID.randomUUID();
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        String title = "Updated Suggestion";
        String description = "Updated description";
        Boolean isAnonymous = true;
        SuggestionStatus status = SuggestionStatus.REJECTED;
        String rejectionReason = "Not feasible";
        LocalDateTime createdAt = LocalDateTime.now();
        Set<SuggestionTag> tags = new HashSet<>();

        suggestion.setId(id);
        suggestion.setUser(user);
        suggestion.setTitle(title);
        suggestion.setDescription(description);
        suggestion.setIsAnonymous(isAnonymous);
        suggestion.setStatus(status);
        suggestion.setRejectionReason(rejectionReason);
        suggestion.setCreatedAt(createdAt);
        suggestion.setTags(tags);

        Assertions.assertEquals(id, suggestion.getId());
        Assertions.assertEquals(user, suggestion.getUser());
        Assertions.assertEquals(title, suggestion.getTitle());
        Assertions.assertEquals(description, suggestion.getDescription());
        Assertions.assertEquals(isAnonymous, suggestion.getIsAnonymous());
        Assertions.assertEquals(status, suggestion.getStatus());
        Assertions.assertEquals(rejectionReason, suggestion.getRejectionReason());
        Assertions.assertEquals(createdAt, suggestion.getCreatedAt());
        Assertions.assertEquals(tags, suggestion.getTags());
    }

    @Test
    void hasCorrectDefaultValues() {
        Suggestion suggestion = new Suggestion();

        Assertions.assertNull(suggestion.getId());
        Assertions.assertNull(suggestion.getUser());
        Assertions.assertNull(suggestion.getTitle());
        Assertions.assertNull(suggestion.getDescription());
        Assertions.assertNull(suggestion.getIsAnonymous());
        Assertions.assertNull(suggestion.getStatus());
        Assertions.assertNull(suggestion.getRejectionReason());
        Assertions.assertNull(suggestion.getCreatedAt());
        Assertions.assertNotNull(suggestion.getTags());
        Assertions.assertTrue(suggestion.getTags().isEmpty());
    }

    @Test
    void onCreateSetsCreatedAtWhenNull() {
        Suggestion suggestion = new Suggestion();
        suggestion.setCreatedAt(null);

        Assertions.assertNull(suggestion.getCreatedAt());
        suggestion.onCreate();
        Assertions.assertNotNull(suggestion.getCreatedAt());
    }

    @Test
    void onCreateDoesNotOverrideExistingCreatedAt() {
        LocalDateTime fixed = LocalDateTime.of(2025, 10, 17, 10, 0);
        Suggestion suggestion = new Suggestion();
        suggestion.setCreatedAt(fixed);

        suggestion.onCreate();
        Assertions.assertEquals(fixed, suggestion.getCreatedAt());
    }

    @Test
    void canHandleNullValues() {
        Suggestion suggestion = new Suggestion();

        suggestion.setUser(null);
        suggestion.setTitle(null);
        suggestion.setDescription(null);
        suggestion.setIsAnonymous(null);
        suggestion.setStatus(null);
        suggestion.setRejectionReason(null);
        suggestion.setCreatedAt(null);
        suggestion.setTags(null);

        Assertions.assertNull(suggestion.getUser());
        Assertions.assertNull(suggestion.getTitle());
        Assertions.assertNull(suggestion.getDescription());
        Assertions.assertNull(suggestion.getIsAnonymous());
        Assertions.assertNull(suggestion.getStatus());
        Assertions.assertNull(suggestion.getRejectionReason());
        Assertions.assertNull(suggestion.getCreatedAt());
        Assertions.assertNull(suggestion.getTags());
    }

    @Test
    void canHandleEmptyStringValues() {
        Suggestion suggestion = new Suggestion();

        suggestion.setTitle("");
        suggestion.setDescription("");
        suggestion.setRejectionReason("");

        Assertions.assertEquals("", suggestion.getTitle());
        Assertions.assertEquals("", suggestion.getDescription());
        Assertions.assertEquals("", suggestion.getRejectionReason());
    }

    @Test
    void canHandleSpecialCharactersInTextFields() {
        Suggestion suggestion = new Suggestion();
        String titleWithSpecialChars = "Title with special chars @#$%^&*()";
        String descriptionWithSpecialChars = "Description with special chars @#$%^&*()";
        String rejectionReasonWithSpecialChars = "Rejection reason with special chars @#$%^&*()";

        suggestion.setTitle(titleWithSpecialChars);
        suggestion.setDescription(descriptionWithSpecialChars);
        suggestion.setRejectionReason(rejectionReasonWithSpecialChars);

        Assertions.assertEquals(titleWithSpecialChars, suggestion.getTitle());
        Assertions.assertEquals(descriptionWithSpecialChars, suggestion.getDescription());
        Assertions.assertEquals(rejectionReasonWithSpecialChars, suggestion.getRejectionReason());
    }

    @Test
    void canHandleLongTextFields() {
        Suggestion suggestion = new Suggestion();
        String longTitle = "A".repeat(1000);
        String longDescription = "A".repeat(5000);
        String longRejectionReason = "A".repeat(2000);

        suggestion.setTitle(longTitle);
        suggestion.setDescription(longDescription);
        suggestion.setRejectionReason(longRejectionReason);

        Assertions.assertEquals(longTitle, suggestion.getTitle());
        Assertions.assertEquals(longDescription, suggestion.getDescription());
        Assertions.assertEquals(longRejectionReason, suggestion.getRejectionReason());
    }

    @Test
    void canHandleAllSuggestionStatuses() {
        for (SuggestionStatus status : SuggestionStatus.values()) {
            Suggestion suggestion = new Suggestion();
            suggestion.setStatus(status);
            
            Assertions.assertEquals(status, suggestion.getStatus());
        }
    }

    @Test
    void canHandleTagsCollection() {
        Suggestion suggestion = new Suggestion();
        Set<SuggestionTag> tags = new HashSet<>();
        SuggestionTag tag1 = Fixtures.suggestionTag(suggestion, "tag1");
        SuggestionTag tag2 = Fixtures.suggestionTag(suggestion, "tag2");
        tags.add(tag1);
        tags.add(tag2);

        suggestion.setTags(tags);

        Assertions.assertEquals(2, suggestion.getTags().size());
        Assertions.assertTrue(suggestion.getTags().contains(tag1));
        Assertions.assertTrue(suggestion.getTags().contains(tag2));
    }

    @Test
    void canHandleEmptyTagsCollection() {
        Suggestion suggestion = new Suggestion();
        Set<SuggestionTag> emptyTags = new HashSet<>();

        suggestion.setTags(emptyTags);

        Assertions.assertTrue(suggestion.getTags().isEmpty());
    }

    @Test
    void canHandleBooleanValues() {
        Suggestion suggestion = new Suggestion();

        suggestion.setIsAnonymous(true);
        Assertions.assertTrue(suggestion.getIsAnonymous());

        suggestion.setIsAnonymous(false);
        Assertions.assertFalse(suggestion.getIsAnonymous());
    }
}
