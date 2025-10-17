package pl.su.su_backend.model.budget;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.testsupport.Fixtures;

import java.time.LocalDateTime;

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
	}

	@Test
	void builderSetsClassIdYearAndCreatedBy() {
		Classes schoolClass = Fixtures.schoolClass("4C", "2025/26");
		Users creator = Fixtures.simpleUser("Piotr Test", "piotr@example.com");

		ClassBudget budget = ClassBudget.builder()
                .classes(schoolClass)
				.year("2025/26")
				.createdBy(creator)
				.build();

		Assertions.assertEquals(schoolClass, budget.getClasses());
        Assertions.assertEquals("2025/26", budget.getYear());
        Assertions.assertEquals(creator, budget.getCreatedBy());
	}

	@Test
	void canChangeFieldsViaSetters() {
		Classes schoolClass = Fixtures.schoolClass("1D", "2022/23");
		Users creator = Fixtures.simpleUser("Tester", "tester@example.com");

		ClassBudget budget = new ClassBudget();
		budget.setClasses(schoolClass);
		budget.setYear("2022/23");
		budget.setCreatedBy(creator);

		Assertions.assertEquals(schoolClass, budget.getClasses());
		Assertions.assertEquals("2022/23", budget.getYear());
		Assertions.assertSame(creator, budget.getCreatedBy());
	}
}
