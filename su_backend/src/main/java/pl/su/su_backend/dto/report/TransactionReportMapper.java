package pl.su.su_backend.dto.report;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.su.su_backend.model.budget.CouncilTransaction;
import pl.su.su_backend.model.users.Users;

@Mapper(componentModel = "spring")
public interface TransactionReportMapper {

    @Mapping(target = "category", constant = "Brak kategorii")
    @Mapping(target = "type", source = "transaction.type")
    @Mapping(target = "transactionDate", expression = "java(transaction.getDate().toLocalDate())")
    @Mapping(target = "createdBy", source = "user.fullName")
    @Mapping(target = "payerUser", ignore = true)
    TransactionSummaryDto toSummaryDto(CouncilTransaction transaction, Users user);
}