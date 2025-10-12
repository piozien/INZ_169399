package pl.su.su_backend.dto.budget;

import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.model.budget.CouncilTransaction;
import pl.su.su_backend.model.users.Users;

import java.util.ArrayList;
import java.util.List;

public class CouncilTransactionMapper {

    private CouncilTransactionMapper() {}

    public static CouncilTransactionResponseDto toResponse(CouncilTransaction transaction) {
        return CouncilTransactionResponseDto.builder()
                .id(transaction.getId())
                .budgetId(transaction.getBudget() != null ? transaction.getBudget().getId() : null)
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .addedById(transaction.getAddedBy() != null ? transaction.getAddedBy().getId() : null)
                .confirmed(transaction.getConfirmed())
                .build();
    }

    public static List<CouncilTransactionResponseDto> toResponseList(List<CouncilTransaction> transactions) {
        List<CouncilTransactionResponseDto> result = new ArrayList<>();
        for (CouncilTransaction transaction : transactions) {
            result.add(toResponse(transaction));
        }
        return result;
    }

    public static CouncilTransaction toEntity(CouncilTransactionRequestDto dto, CouncilBudget budget, Users addedBy) {
        return CouncilTransaction.builder()
                .budget(budget)
                .type(dto.getType())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .date(dto.getDate())
                .addedBy(addedBy)
                .confirmed(false)
                .build();
    }
}
