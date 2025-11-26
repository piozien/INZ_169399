package pl.su.su_backend.dto.budget;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.su.su_backend.model.budget.CouncilTransaction;
import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.model.users.Users;

@Mapper(componentModel = "spring")
public interface CouncilTransactionMapper {

    @Mapping(target = "budgetId", source = "budget.id")
    @Mapping(target = "addedById", source = "addedBy.id")
    CouncilTransactionResponseDto toResponse(CouncilTransaction transaction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "budget", source = "budget")
    @Mapping(target = "addedBy", source = "addedBy")
    @Mapping(target = "type", source = "dto.type")
    @Mapping(target = "amount", source = "dto.amount")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "date", source = "dto.date")
    CouncilTransaction toEntity(CouncilTransactionRequestDto dto, CouncilBudget budget, Users addedBy);
}