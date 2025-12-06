package pl.su.su_backend.dto.suggestion;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.su.su_backend.model.suggestion.Suggestion;
import pl.su.su_backend.model.suggestion.SuggestionTag;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SuggestionMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapTagsToStrings")
    @Mapping(target = "councilId", source = "council.id")
    SuggestionResponseDto toResponse(Suggestion suggestion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "council", ignore = true)
    Suggestion toEntity(SuggestionRequestDto dto);

    @Named("mapTagsToStrings")
    default Set<String> mapTagsToStrings(Set<SuggestionTag> tags) {
        if (tags == null) return Set.of();
        return tags.stream()
                .map(tag -> tag.getId().getTag())
                .collect(Collectors.toSet());
    }
}