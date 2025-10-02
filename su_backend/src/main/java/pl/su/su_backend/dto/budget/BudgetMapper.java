package pl.su.su_backend.dto.budget;

import pl.su.su_backend.model.budget.*;

public class BudgetMapper {

	private BudgetMapper() {}

	public static ClassBudgetResponseDto toResponseDto(ClassBudget classBudget) {
		return ClassBudgetResponseDto.builder()
				.id(classBudget.getId())
				.classId(classBudget.getClassId())
				.year(classBudget.getYear())
				.createdById(classBudget.getCreatedBy() != null ? classBudget.getCreatedBy().getId() : null)
				.createdAt(classBudget.getCreatedAt())
				.build();
	}

	public static CouncilBudgetResponseDto toResponseDto(CouncilBudget councilBudget) {
		return CouncilBudgetResponseDto.builder()
				.id(councilBudget.getId())
				.councilId(councilBudget.getCouncilId())
				.year(councilBudget.getYear())
				.createdById(councilBudget.getCreatedBy() != null ? councilBudget.getCreatedBy().getId() : null)
				.createdAt(councilBudget.getCreatedAt())
				.build();
	}

	public static ClassTransactionResponseDto toResponseDto(ClassTransaction classTransaction) {
		return ClassTransactionResponseDto.builder()
				.id(classTransaction.getId())
				.budgetId(classTransaction.getBudget() != null ? classTransaction.getBudget().getId() : null)
				.type(classTransaction.getType())
				.amount(classTransaction.getAmount())
				.description(classTransaction.getDescription())
				.date(classTransaction.getDate())
				.addedById(classTransaction.getAddedBy() != null ? classTransaction.getAddedBy().getId() : null)
				.payerUser(classTransaction.getPayerUser())
				.confirmed(classTransaction.getConfirmed())
				.build();
	}

	public static CouncilTransactionResponseDto toResponseDto(CouncilTransaction councilTransactionx) {
		return CouncilTransactionResponseDto.builder()
				.id(councilTransactionx.getId())
				.budgetId(councilTransactionx.getBudget() != null ? councilTransactionx.getBudget().getId() : null)
				.type(councilTransactionx.getType())
				.amount(councilTransactionx.getAmount())
				.description(councilTransactionx.getDescription())
				.date(councilTransactionx.getDate())
				.addedById(councilTransactionx.getAddedBy() != null ? councilTransactionx.getAddedBy().getId() : null)
				.confirmed(councilTransactionx.getConfirmed())
				.build();
	}
}
