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
                .councilName(budget.getCouncil() != null ? budget.getCouncil().getName() : null)
                .year(budget.getYear())
                .initialAmount(budget.getInitialAmount())
                .balance(budget.getBalance())
                .createdById(budget.getCreatedBy() != null ? budget.getCreatedBy().getId() : null)
                .createdAt(budget.getCreatedAt())
                .build();
    }

}
