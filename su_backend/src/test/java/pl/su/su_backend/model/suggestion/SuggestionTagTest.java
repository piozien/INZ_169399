package pl.su.su_backend.model.suggestion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.enums.SuggestionStatus;
import pl.su.su_backend.testsupport.Fixtures;

import java.util.UUID;

public class SuggestionTagTest {

    @Test
    void builderSetsAllFields() {
        Suggestion suggestion = Fixtures.suggestion(
                Fixtures.simpleUser("Test User", "test@example.com"),
                "Test Suggestion",
                "Test description",
                SuggestionStatus.PENDING
        );
        String tagName = "test-tag";
        SuggestionTag.Id id = new SuggestionTag.Id(suggestion.getId(), tagName);

        SuggestionTag suggestionTag = SuggestionTag.builder()
                .id(id)
                .suggestion(suggestion)
                .build();

        Assertions.assertEquals(id, suggestionTag.getId());
        Assertions.assertEquals(suggestion, suggestionTag.getSuggestion());
        Assertions.assertEquals(tagName, suggestionTag.getId().getTag());
    }

    @Test
    void canChangeFieldsViaSetters() {
        SuggestionTag suggestionTag = new SuggestionTag();
        Suggestion suggestion = Fixtures.suggestion(
                Fixtures.simpleUser("Test User", "test@example.com"),
                "Test Suggestion",
                "Test description",
                SuggestionStatus.PENDING
        );
        String tagName = "updated-tag";
        SuggestionTag.Id id = new SuggestionTag.Id(suggestion.getId(), tagName);

        suggestionTag.setId(id);
        suggestionTag.setSuggestion(suggestion);

        Assertions.assertEquals(id, suggestionTag.getId());
        Assertions.assertEquals(suggestion, suggestionTag.getSuggestion());
        Assertions.assertEquals(tagName, suggestionTag.getId().getTag());
    }

    @Test
    void hasCorrectDefaultValues() {
        SuggestionTag suggestionTag = new SuggestionTag();

        Assertions.assertNull(suggestionTag.getId());
        Assertions.assertNull(suggestionTag.getSuggestion());
    }

    @Test
    void canHandleNullValues() {
        SuggestionTag suggestionTag = new SuggestionTag();

        suggestionTag.setSuggestion(null);
        suggestionTag.setId(null);

        Assertions.assertNull(suggestionTag.getSuggestion());
        Assertions.assertNull(suggestionTag.getId());
    }

    @Test
    void canHandleEmptyStringTagName() {
        SuggestionTag suggestionTag = new SuggestionTag();
        SuggestionTag.Id id = new SuggestionTag.Id(UUID.randomUUID(), "");

        suggestionTag.setId(id);

        Assertions.assertEquals("", suggestionTag.getId().getTag());
    }

    @Test
    void canHandleSpecialCharactersInTagName() {
        SuggestionTag suggestionTag = new SuggestionTag();
        String tagNameWithSpecialChars = "@#$%^&*()";
        SuggestionTag.Id id = new SuggestionTag.Id(UUID.randomUUID(), tagNameWithSpecialChars);

        suggestionTag.setId(id);

        Assertions.assertEquals(tagNameWithSpecialChars, suggestionTag.getId().getTag());
    }

    @Test
    void canHandleLongTagName() {
        SuggestionTag suggestionTag = new SuggestionTag();
        String longTagName = "A".repeat(1000);
        SuggestionTag.Id id = new SuggestionTag.Id(UUID.randomUUID(), longTagName);

        suggestionTag.setId(id);

        Assertions.assertEquals(longTagName, suggestionTag.getId().getTag());
    }
}
