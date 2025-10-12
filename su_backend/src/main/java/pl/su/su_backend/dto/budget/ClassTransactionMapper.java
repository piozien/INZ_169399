package pl.su.su_backend.dto.budget;

import pl.su.su_backend.model.budget.ClassTransaction;

public class ClassTransactionMapper {
    
    public static ClassTransactionResponseDto toResponse(ClassTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return ClassTransactionResponseDto.builder()
                .id(transaction.getId())
                .budgetId(transaction.getBudget().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .addedById(transaction.getAddedBy().getId())
                .addedByFullName(transaction.getAddedBy().getFullName())
                .payerUserId(transaction.getPayerUser() != null ? transaction.getPayerUser().getId() : null)
                .payerUserFullName(transaction.getPayerUser() != null ? transaction.getPayerUser().getFullName() : null)
                .confirmed(transaction.getConfirmed())
                .build();
    }

}
