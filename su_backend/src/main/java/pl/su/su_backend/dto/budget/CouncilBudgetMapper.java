package pl.su.su_backend.dto.budget;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.model.budget.CouncilTransaction;
import pl.su.su_backend.model.enums.TransactionType;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CouncilBudgetMapper {

    @Mapping(target = "councilId", source = "council.id")
    @Mapping(target = "councilName", source = "council.name")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "totalIncome", ignore = true)
    @Mapping(target = "totalExpenses", ignore = true)
    CouncilBudgetResponseDto toResponse(CouncilBudget budget);

    @AfterMapping
    default void calculateTotals(CouncilBudget source, @MappingTarget CouncilBudgetResponseDto target) {
        if (source.getTransactions() == null) {
            target.setTotalIncome(BigDecimal.ZERO);
            target.setTotalExpenses(BigDecimal.ZERO);
            return;
        }

        BigDecimal income = source.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(CouncilTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenses = source.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(CouncilTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        target.setTotalIncome(income);
        target.setTotalExpenses(expenses);
    }
}