package pl.su.su_backend.model.budget;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.testsupport.Fixtures;

import java.math.BigDecimal;

public class ClassTransactionTest {

    @Test
    void builderSetsAllFields() {
        Users user = Fixtures.simpleUser("Piotr Test", "piotr@test.com");
        Classes schoolClass = Fixtures.schoolClass("1D", "2025/26");
        ClassBudget classBudget = Fixtures.classBudget(schoolClass, new BigDecimal("100.0"), user);

        ClassTransaction classTransaction = ClassTransaction.builder()
                .budget(classBudget)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("50.0"))
                .description("Test expense transaction")
                .addedBy(user)
                .payerUser(user)
                .build();

        Assertions.assertEquals(classBudget, classTransaction.getBudget());
        Assertions.assertEquals(TransactionType.EXPENSE, classTransaction.getType());
        Assertions.assertEquals(new BigDecimal("50.0"), classTransaction.getAmount());
        Assertions.assertEquals("Test expense transaction", classTransaction.getDescription());
        Assertions.assertEquals(user, classTransaction.getAddedBy());
        Assertions.assertEquals(user, classTransaction.getPayerUser());
    }

    @Test
    void canCreateIncomeTransaction() {
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        Classes schoolClass = Fixtures.schoolClass("2A", "2025/26");
        ClassBudget classBudget = Fixtures.classBudget(schoolClass, new BigDecimal("200.0"), user);

        ClassTransaction transaction = ClassTransaction.builder()
                .budget(classBudget)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100.0"))
                .description("Class")
                .addedBy(user)
                .build();

        Assertions.assertEquals(TransactionType.INCOME, transaction.getType());
        Assertions.assertEquals(new BigDecimal("100.0"), transaction.getAmount());
        Assertions.assertEquals("Class", transaction.getDescription());
    }

    @Test
    void canCreateExpenseTransaction() {
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        Classes schoolClass = Fixtures.schoolClass("3B", "2025/26");
        ClassBudget classBudget = Fixtures.classBudget(schoolClass, new BigDecimal("300.0"), user);

        ClassTransaction transaction = ClassTransaction.builder()
                .budget(classBudget)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("75.0"))
                .description("Class trip expenses")
                .addedBy(user)
                .build();

        Assertions.assertEquals(TransactionType.EXPENSE, transaction.getType());
        Assertions.assertEquals(new BigDecimal("75.0"), transaction.getAmount());
        Assertions.assertEquals("Class trip expenses", transaction.getDescription());
    }

    @Test
    void canSetPayerUser() {
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        Users payer = Fixtures.simpleUser("Payer User", "payer@example.com");
        Classes schoolClass = Fixtures.schoolClass("4C", "2025/26");
        ClassBudget classBudget = Fixtures.classBudget(schoolClass, new BigDecimal("150.0"), user);

        ClassTransaction transaction = ClassTransaction.builder()
                .budget(classBudget)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("25.0"))
                .description("Individual payment")
                .addedBy(user)
                .payerUser(payer)
                .build();

        Assertions.assertEquals(payer, transaction.getPayerUser());
    }

    @Test
    void payerUserCanBeNull() {
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        Classes schoolClass = Fixtures.schoolClass("5D", "2025/26");
        ClassBudget classBudget = Fixtures.classBudget(schoolClass, new BigDecimal("100.0"), user);

        ClassTransaction transaction = ClassTransaction.builder()
                .budget(classBudget)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("50.0"))
                .description("General income")
                .addedBy(user)
                .build();

        Assertions.assertNull(transaction.getPayerUser());
    }

    @Test
    void canChangeFieldsViaSetters() {
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        Users payer = Fixtures.simpleUser("Payer User", "payer@example.com");
        Classes schoolClass = Fixtures.schoolClass("6E", "2025/26");
        ClassBudget classBudget = Fixtures.classBudget(schoolClass, new BigDecimal("200.0"), user);

        ClassTransaction transaction = new ClassTransaction();
        transaction.setBudget(classBudget);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setAmount(new BigDecimal("30.0"));
        transaction.setDescription("Updated description");
        transaction.setAddedBy(user);
        transaction.setPayerUser(payer);

        Assertions.assertEquals(classBudget, transaction.getBudget());
        Assertions.assertEquals(TransactionType.EXPENSE, transaction.getType());
        Assertions.assertEquals(new BigDecimal("30.0"), transaction.getAmount());
        Assertions.assertEquals("Updated description", transaction.getDescription());
        Assertions.assertEquals(user, transaction.getAddedBy());
        Assertions.assertEquals(payer, transaction.getPayerUser());
    }

    @Test
    void hasCorrectDefaultValues() {
        ClassTransaction transaction = new ClassTransaction();

        Assertions.assertNull(transaction.getId());
        Assertions.assertNull(transaction.getBudget());
        Assertions.assertNull(transaction.getType());
        Assertions.assertNull(transaction.getAmount());
        Assertions.assertNull(transaction.getDescription());
        Assertions.assertNull(transaction.getAddedBy());
        Assertions.assertNull(transaction.getPayerUser());
    }

    @Test
    void canHandleZeroAmount() {
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        Classes schoolClass = Fixtures.schoolClass("7F", "2025/26");
        ClassBudget classBudget = Fixtures.classBudget(schoolClass, new BigDecimal("100.0"), user);

        ClassTransaction transaction = ClassTransaction.builder()
                .budget(classBudget)
                .type(TransactionType.INCOME)
                .amount(BigDecimal.ZERO)
                .description("Zero amount transaction")
                .addedBy(user)
                .build();

        Assertions.assertEquals(BigDecimal.ZERO, transaction.getAmount());
    }

    @Test
    void canHandleNegativeAmount() {
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        Classes schoolClass = Fixtures.schoolClass("8G", "2025/26");
        ClassBudget classBudget = Fixtures.classBudget(schoolClass, new BigDecimal("100.0"), user);

        ClassTransaction transaction = ClassTransaction.builder()
                .budget(classBudget)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("-50.0"))
                .description("Refund transaction")
                .addedBy(user)
                .build();

        Assertions.assertEquals(new BigDecimal("-50.0"), transaction.getAmount());
    }

    @Test
    void canCreateTransactionWithSpecialCharactersInDescription() {
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        Classes schoolClass = Fixtures.schoolClass("9H", "2025/26");
        ClassBudget classBudget = Fixtures.classBudget(schoolClass, new BigDecimal("100.0"), user);

        ClassTransaction transaction = ClassTransaction.builder()
                .budget(classBudget)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("25.0"))
                .description("!@#$%^&*()")
                .addedBy(user)
                .build();

        Assertions.assertEquals("!@#$%^&*()", transaction.getDescription());
    }
}
