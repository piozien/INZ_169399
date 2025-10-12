package pl.su.su_backend.dto.budget;

import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.users.Users;

import java.util.ArrayList;
import java.util.List;

public class CouncilBudgetMapper {

    private CouncilBudgetMapper() {}

    public static CouncilBudgetResponseDto toResponse(CouncilBudget budget) {
        return CouncilBudgetResponseDto.builder()
                .id(budget.getId())
                .councilId(budget.getCouncil() != null ? budget.getCouncil().getId() : null)
                .year(budget.getYear())
                .initialAmount(budget.getInitialAmount())
                .createdById(budget.getCreatedBy() != null ? budget.getCreatedBy().getId() : null)
                .createdAt(budget.getCreatedAt())
                .build();
    }

    public static List<CouncilBudgetResponseDto> toResponseList(List<CouncilBudget> budgets) {
        List<CouncilBudgetResponseDto> result = new ArrayList<>();
        for (CouncilBudget budget : budgets) {
            result.add(toResponse(budget));
        }
        return result;
    }

    public static CouncilBudget toEntity(CouncilBudgetRequestDto dto, Council council, Users createdBy) {
        return CouncilBudget.builder()
                .council(council)
                .year(dto.getYear())
                .initialAmount(dto.getInitialAmount())
                .createdBy(createdBy)
                .build();
    }
}
