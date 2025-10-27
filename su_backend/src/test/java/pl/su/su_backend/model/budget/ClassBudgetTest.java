package pl.su.su_backend.model.budget;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.testsupport.Fixtures;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ClassBudgetTest {

    @Test
	void onCreateSetsCreatedAtWhenNull() {
		Classes schoolClass = Fixtures.schoolClass("3A", "2025/26");
		Users creator = Fixtures.simpleUser("Test User", "user@example.com");

		ClassBudget budget = ClassBudget.builder()
				.classes(schoolClass)
				.createdBy(creator)
				.year("2025/26")
				.build();

		Assertions.assertNull(budget.getCreatedAt());
		budget.onCreate();
		Assertions.assertNotNull(budget.getCreatedAt());
	}

	@Test
	void onCreateDoesNotOverrideExistingCreatedAt() {
		LocalDateTime fixed = LocalDateTime.of(2025, 10, 17, 10, 0);
		Classes schoolClass = Fixtures.schoolClass("3B", "2025/26");
		Users creator = Fixtures.simpleUser("Test User", "user@example.com");

		ClassBudget budget = ClassBudget.builder()
				.classes(schoolClass)
				.createdBy(creator)
				.year("2025/26")
				.createdAt(fixed)
				.build();

		budget.onCreate();
		Assertions.assertEquals(fixed, budget.getCreatedAt());
        Assertions.assertEquals(BigDecimal.ZERO, budget.getBalance());
	}

	@Test
	void onCreateSetsBalanceToInitialAmountWhenBalanceIsNull() {
		Classes schoolClass = Fixtures.schoolClass("4C", "2025/26");
		Users creator = Fixtures.simpleUser("Test User", "user@example.com");

		ClassBudget budget = ClassBudget.builder()
				.classes(schoolClass)
				.createdBy(creator)
				.year("2025/26")
				.initialAmount(new BigDecimal("100.50"))
				.build();

		Assertions.assertNull(budget.getBalance());
		budget.onCreate();
		Assertions.assertEquals(new BigDecimal("100.50"), budget.getBalance());
	}

	@Test
	void onCreateSetsBalanceToZeroWhenInitialAmountIsNull() {
		Classes schoolClass = Fixtures.schoolClass("5D", "2025/26");
		Users creator = Fixtures.simpleUser("Test User", "user@example.com");

		ClassBudget budget = ClassBudget.builder()
				.classes(schoolClass)
				.createdBy(creator)
				.year("2025/26")
				.build();

		Assertions.assertNull(budget.getBalance());
		budget.onCreate();
		Assertions.assertEquals(BigDecimal.ZERO, budget.getBalance());
	}

	@Test
	void onCreateDoesNotOverrideExistingBalance() {
		Classes schoolClass = Fixtures.schoolClass("6E", "2025/26");
		Users creator = Fixtures.simpleUser("Test User", "user@example.com");

		ClassBudget budget = ClassBudget.builder()
				.classes(schoolClass)
				.createdBy(creator)
				.year("2025/26")
				.initialAmount(new BigDecimal("100.00"))
				.balance(new BigDecimal("50.00"))
				.build();

		budget.onCreate();
		Assertions.assertEquals(new BigDecimal("50.00"), budget.getBalance());
	}

	@Test
	void builderSetsAllFields() {
		UUID id = UUID.randomUUID();
		Classes schoolClass = Fixtures.schoolClass("7F", "2025/26");
		Users creator = Fixtures.simpleUser("Test User", "user@example.com");
		LocalDateTime now = LocalDateTime.now();

		ClassBudget budget = ClassBudget.builder()
				.id(id)
				.classes(schoolClass)
				.year("2025/26")
				.initialAmount(new BigDecimal("200.00"))
				.balance(new BigDecimal("150.00"))
				.createdBy(creator)
				.createdAt(now)
				.build();

		Assertions.assertEquals(id, budget.getId());
		Assertions.assertEquals(schoolClass, budget.getClasses());
		Assertions.assertEquals("2025/26", budget.getYear());
		Assertions.assertEquals(new BigDecimal("200.00"), budget.getInitialAmount());
		Assertions.assertEquals(new BigDecimal("150.00"), budget.getBalance());
		Assertions.assertEquals(creator, budget.getCreatedBy());
		Assertions.assertEquals(now, budget.getCreatedAt());
	}

	@Test
	void canChangeFieldsViaSetters() {
		Classes schoolClass = Fixtures.schoolClass("8G", "2025/26");
		Users creator = Fixtures.simpleUser("Tester", "tester@example.com");
		LocalDateTime now = LocalDateTime.now();

		ClassBudget budget = new ClassBudget();
		budget.setClasses(schoolClass);
		budget.setYear("2025/26");
		budget.setInitialAmount(new BigDecimal("300.00"));
		budget.setBalance(new BigDecimal("250.00"));
		budget.setCreatedBy(creator);
		budget.setCreatedAt(now);

		Assertions.assertEquals(schoolClass, budget.getClasses());
		Assertions.assertEquals("2025/26", budget.getYear());
		Assertions.assertEquals(new BigDecimal("300.00"), budget.getInitialAmount());
		Assertions.assertEquals(new BigDecimal("250.00"), budget.getBalance());
		Assertions.assertSame(creator, budget.getCreatedBy());
		Assertions.assertEquals(now, budget.getCreatedAt());
	}

	@Test
	void hasCorrectDefaultValues() {
		ClassBudget budget = new ClassBudget();
		budget.onCreate();

		Assertions.assertNull(budget.getId());
		Assertions.assertNull(budget.getClasses());
		Assertions.assertNull(budget.getYear());
		Assertions.assertNull(budget.getInitialAmount());
		Assertions.assertEquals(BigDecimal.ZERO, budget.getBalance());
		Assertions.assertNull(budget.getCreatedBy());
		Assertions.assertNotNull(budget.getCreatedAt());
		Assertions.assertNotNull(budget.getTransactions());
		Assertions.assertTrue(budget.getTransactions().isEmpty());
	}

	@Test
	void canHandleZeroAmounts() {
		Classes schoolClass = Fixtures.schoolClass("9H", "2025/26");
		Users creator = Fixtures.simpleUser("Test User", "user@example.com");

		ClassBudget budget = ClassBudget.builder()
				.classes(schoolClass)
				.year("2025/26")
				.createdBy(creator)
				.initialAmount(BigDecimal.ZERO)
				.balance(BigDecimal.ZERO)
				.build();

		budget.onCreate();

		Assertions.assertEquals(BigDecimal.ZERO, budget.getInitialAmount());
		Assertions.assertEquals(BigDecimal.ZERO, budget.getBalance());
	}

	@Test
	void canHandleNegativeAmounts() {
		Classes schoolClass = Fixtures.schoolClass("10I", "2025/26");
		Users creator = Fixtures.simpleUser("Test User", "user@example.com");

		ClassBudget budget = ClassBudget.builder()
				.classes(schoolClass)
				.year("2025/26")
				.createdBy(creator)
				.initialAmount(new BigDecimal("-100.00"))
				.balance(new BigDecimal("-50.00"))
				.build();

		budget.onCreate();

		Assertions.assertEquals(new BigDecimal("-100.00"), budget.getInitialAmount());
		Assertions.assertEquals(new BigDecimal("-50.00"), budget.getBalance());
	}

}
