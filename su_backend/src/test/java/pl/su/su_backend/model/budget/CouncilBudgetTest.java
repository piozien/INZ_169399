package pl.su.su_backend.model.budget;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.time.LocalDateTime;

public class CouncilBudgetTest {

    @Test
    void hasCorrectDefaultValues() {
        CouncilBudget councilBudget = new CouncilBudget();
        Assertions.assertNull(councilBudget.getYear());
        Assertions.assertNull(councilBudget.getInitialAmount());
        Assertions.assertNull(councilBudget.getCreatedAt());
        councilBudget.onCreate();
        Assertions.assertNotNull(councilBudget.getCreatedAt());

    }
}
