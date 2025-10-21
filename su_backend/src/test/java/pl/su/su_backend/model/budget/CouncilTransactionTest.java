package pl.su.su_backend.model.budget;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.testsupport.Fixtures;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CouncilTransactionTest {

    @Test
    void builderSetsAllFields() {
        UUID id = UUID.randomUUID();
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .build();
        LocalDateTime now = LocalDateTime.now();

        CouncilTransaction transaction = CouncilTransaction.builder()
                .id(id)
                .budget(budget)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100.50"))
                .description("Test income transaction")
                .date(now)
                .addedBy(user)
                .build();

        Assertions.assertEquals(id, transaction.getId());
        Assertions.assertEquals(budget, transaction.getBudget());
        Assertions.assertEquals(TransactionType.INCOME, transaction.getType());
        Assertions.assertEquals(new BigDecimal("100.50"), transaction.getAmount());
        Assertions.assertEquals("Test income transaction", transaction.getDescription());
        Assertions.assertEquals(now, transaction.getDate());
        Assertions.assertEquals(user, transaction.getAddedBy());
    }

    @Test
    void canChangeFieldsViaSetters() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .build();
        LocalDateTime now = LocalDateTime.now();

        CouncilTransaction transaction = new CouncilTransaction();
        transaction.setBudget(budget);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setAmount(new BigDecimal("50.25"));
        transaction.setDescription("Test expense transaction");
        transaction.setDate(now);
        transaction.setAddedBy(user);

        Assertions.assertEquals(budget, transaction.getBudget());
        Assertions.assertEquals(TransactionType.EXPENSE, transaction.getType());
        Assertions.assertEquals(new BigDecimal("50.25"), transaction.getAmount());
        Assertions.assertEquals("Test expense transaction", transaction.getDescription());
        Assertions.assertEquals(now, transaction.getDate());
        Assertions.assertEquals(user, transaction.getAddedBy());
    }

    @Test
    void hasCorrectDefaultValues() {
        CouncilTransaction transaction = new CouncilTransaction();

        Assertions.assertNull(transaction.getId());
        Assertions.assertNull(transaction.getBudget());
        Assertions.assertNull(transaction.getType());
        Assertions.assertNull(transaction.getAmount());
        Assertions.assertNull(transaction.getDescription());
        Assertions.assertNull(transaction.getDate());
        Assertions.assertNull(transaction.getAddedBy());
    }

    @Test
    void canCreateIncomeTransaction() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .build();

        CouncilTransaction transaction = CouncilTransaction.builder()
                .budget(budget)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("200.00"))
                .description("test")
                .date(LocalDateTime.now())
                .addedBy(user)
                .build();

        Assertions.assertEquals(TransactionType.INCOME, transaction.getType());
        Assertions.assertEquals(new BigDecimal("200.00"), transaction.getAmount());
        Assertions.assertEquals("test", transaction.getDescription());
    }

    @Test
    void canCreateExpenseTransaction() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .build();

        CouncilTransaction transaction = CouncilTransaction.builder()
                .budget(budget)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("75.50"))
                .description("trip")
                .date(LocalDateTime.now())
                .addedBy(user)
                .build();

        Assertions.assertEquals(TransactionType.EXPENSE, transaction.getType());
        Assertions.assertEquals(new BigDecimal("75.50"), transaction.getAmount());
        Assertions.assertEquals("trip", transaction.getDescription());
    }

    @Test
    void canHandleZeroAmount() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .build();

        CouncilTransaction transaction = CouncilTransaction.builder()
                .budget(budget)
                .type(TransactionType.INCOME)
                .amount(BigDecimal.ZERO)
                .description("Zero amount transaction")
                .date(LocalDateTime.now())
                .addedBy(user)
                .build();

        Assertions.assertEquals(BigDecimal.ZERO, transaction.getAmount());
    }

    @Test
    void canHandleNegativeAmount() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .build();

        CouncilTransaction transaction = CouncilTransaction.builder()
                .budget(budget)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("-100.00"))
                .description("Refund transaction")
                .date(LocalDateTime.now())
                .addedBy(user)
                .build();

        Assertions.assertEquals(new BigDecimal("-100.00"), transaction.getAmount());
    }

    @Test
    void canCreateTransactionWithSpecialCharactersInDescription() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .build();

        CouncilTransaction transaction = CouncilTransaction.builder()
                .budget(budget)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100.00"))
                .description("!@#$%^&*()")
                .date(LocalDateTime.now())
                .addedBy(user)
                .build();

        Assertions.assertEquals("!@#$%^&*()", transaction.getDescription());
    }

    @Test
    void canCreateTransactionWithLongDescription() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .build();

        String longDescription = "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                " Cras semper sed elit at egestas. Nullam ullamcorper venenatis aliquet." +
                " Nullam eget nibh nec velit pellentesque porta. Mauris fermentum massa sed posuere tincidunt." +
                " Nunc in imperdiet ante. Cras vulputate efficitur metus, a cursus ex tristique quis. Fusce ac tempor risus." +
                " Donec tempus est non lacus aliquam sagittis.";

        CouncilTransaction transaction = CouncilTransaction.builder()
                .budget(budget)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("250.75"))
                .description(longDescription)
                .date(LocalDateTime.now())
                .addedBy(user)
                .build();

        Assertions.assertEquals(longDescription, transaction.getDescription());
    }

    @Test
    void canCreateTransactionWithDifferentDate() {
        Council council = Fixtures.createCouncil("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year("2025/26")
                .createdBy(user)
                .build();

        LocalDateTime specificDate = LocalDateTime.of(2025, 10, 17, 14, 30);

        CouncilTransaction transaction = CouncilTransaction.builder()
                .budget(budget)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("150.00"))
                .description("Transaction with specific date")
                .date(specificDate)
                .addedBy(user)
                .build();

        Assertions.assertEquals(specificDate, transaction.getDate());
    }
}
