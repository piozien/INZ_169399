package pl.su.su_backend.model.budget;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.testsupport.Fixtures;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CouncilBudgetTest {

    @Test
    void onCreateSetsCreatedAtWhenNull() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .build();

        Assertions.assertNull(budget.getCreatedAt());
        budget.onCreate();
        Assertions.assertNotNull(budget.getCreatedAt());
    }

    @Test
    void onCreateDoesNotOverrideExistingCreatedAt() {
        LocalDateTime fixed = LocalDateTime.of(2025, 10, 17, 10, 0);
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .createdAt(fixed)
                .build();

        budget.onCreate();
        Assertions.assertEquals(fixed, budget.getCreatedAt());
    }

    @Test
    void onCreateSetsBalanceToInitialAmountWhenBalanceIsNull() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .initialAmount(new BigDecimal("100.50"))
                .build();

        Assertions.assertNull(budget.getBalance());
        budget.onCreate();
        Assertions.assertEquals(new BigDecimal("100.50"), budget.getBalance());
    }

    @Test
    void onCreateSetsBalanceToZeroWhenInitialAmountIsNull() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .build();

        Assertions.assertNull(budget.getBalance());
        budget.onCreate();
        Assertions.assertEquals(BigDecimal.ZERO, budget.getBalance());
    }

    @Test
    void onCreateDoesNotOverrideExistingBalance() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .initialAmount(new BigDecimal("100.00"))
                .balance(new BigDecimal("50.00"))
                .build();

        budget.onCreate();
        Assertions.assertEquals(new BigDecimal("50.00"), budget.getBalance());
    }

    @Test
    void builderSetsAllFields() {
        UUID id = UUID.randomUUID();
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        LocalDateTime now = LocalDateTime.now();

        CouncilBudget budget = CouncilBudget.builder()
                .id(id)
                .council(council)
                .year("2025/26")
                .initialAmount(new BigDecimal("500.00"))
                .balance(new BigDecimal("450.00"))
                .createdBy(user)
                .createdAt(now)
                .build();

        Assertions.assertEquals(id, budget.getId());
        Assertions.assertEquals(council, budget.getCouncil());
        Assertions.assertEquals("2025/26", budget.getYear());
        Assertions.assertEquals(new BigDecimal("500.00"), budget.getInitialAmount());
        Assertions.assertEquals(new BigDecimal("450.00"), budget.getBalance());
        Assertions.assertEquals(user, budget.getCreatedBy());
        Assertions.assertEquals(now, budget.getCreatedAt());
    }

    @Test
    void canChangeFieldsViaSetters() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        LocalDateTime now = LocalDateTime.now();

        CouncilBudget budget = new CouncilBudget();
        budget.setCouncil(council);
        budget.setYear("2025/26");
        budget.setInitialAmount(new BigDecimal("300.00"));
        budget.setBalance(new BigDecimal("250.00"));
        budget.setCreatedBy(user);
        budget.setCreatedAt(now);

        Assertions.assertEquals(council, budget.getCouncil());
        Assertions.assertEquals("2025/26", budget.getYear());
        Assertions.assertEquals(new BigDecimal("300.00"), budget.getInitialAmount());
        Assertions.assertEquals(new BigDecimal("250.00"), budget.getBalance());
        Assertions.assertEquals(user, budget.getCreatedBy());
        Assertions.assertEquals(now, budget.getCreatedAt());
    }

    @Test
    void hasCorrectDefaultValues() {
        CouncilBudget budget = new CouncilBudget();
        budget.onCreate();

        Assertions.assertNull(budget.getId());
        Assertions.assertNull(budget.getCouncil());
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
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .initialAmount(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .build();

        budget.onCreate();

        Assertions.assertEquals(BigDecimal.ZERO, budget.getInitialAmount());
        Assertions.assertEquals(BigDecimal.ZERO, budget.getBalance());
    }

    @Test
    void canHandleNegativeAmounts() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .initialAmount(new BigDecimal("-100.00"))
                .balance(new BigDecimal("-50.00"))
                .build();

        budget.onCreate();

        Assertions.assertEquals(new BigDecimal("-100.00"), budget.getInitialAmount());
        Assertions.assertEquals(new BigDecimal("-50.00"), budget.getBalance());
    }


}