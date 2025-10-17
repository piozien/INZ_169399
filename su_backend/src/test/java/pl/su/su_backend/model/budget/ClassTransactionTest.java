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
    void onCreateStatusConfirmedFalse(){
        Users user = Fixtures.simpleUser("Piotr Test", "piotr@test.com");
        Classes schoolClass = Fixtures.schoolClass("1D", "2025/26");
        ClassBudget classBudget = Fixtures.classBudget(schoolClass,new BigDecimal("0.1"), user);

        ClassTransaction classTransaction = ClassTransaction.builder()
                .budget(classBudget)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("0.2"))
                .description("Test expense transaction")
                .addedBy(user)
                .build();

        Assertions.assertEquals(classBudget, classTransaction.getBudget());
        Assertions.assertEquals(TransactionType.EXPENSE, classTransaction.getType());
        Assertions.assertEquals(new BigDecimal("0.2"), classTransaction.getAmount());
        Assertions.assertTrue(classTransaction.getDescription().contains("Test expense"));
        System.out.println(classTransaction.get);
        Assertions.assertEquals(user, classTransaction.getAddedBy());
        Assertions.assertNull(classTransaction.getPayerUser());


    }

    @Test
    void hasCorrectDefaultValues() {
        ClassTransaction transaction = new ClassTransaction();

        Assertions.assertFalse(transaction.getConfirmed());
        Assertions.assertNull(transaction.getPayerUser());
    }
}
