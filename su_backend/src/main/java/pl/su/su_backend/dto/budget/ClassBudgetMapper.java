package pl.su.su_backend.dto.budget;

import pl.su.su_backend.model.budget.ClassBudget;


public class ClassBudgetMapper {
    
    public static ClassBudgetResponseDto toResponse(ClassBudget budget) {
        if (budget == null) {
            return null;
        }
        
        return ClassBudgetResponseDto.builder()
                .id(budget.getId())
                .classId(budget.getClasses().getId())
                .className(budget.getClasses().getName())
                .year(budget.getYear())
                .initialAmount(budget.getInitialAmount())
                .balance(budget.getBalance())
                .createdById(budget.getCreatedBy().getId())
                .createdByFullName(budget.getCreatedBy().getFullName())
                .createdAt(budget.getCreatedAt())
                .build();
    }

}
